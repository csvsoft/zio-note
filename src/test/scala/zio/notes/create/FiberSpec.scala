package zio.notes.create


import java.util.concurrent.TimeUnit

import zio.Cause.Interrupt
import zio.Fiber.Id
import zio.{FiberRef, Schedule, UIO, ZIO}

class FiberSpec extends BasicSpec {


  def fib(n: Long): UIO[Long] = {
    val x: UIO[Long] = if (n <= 2) UIO.succeed(1)
    else for {
      fiba <- fib(n - 1).fork
      fibb <- fib(n - 2).fork

      a <- fiba.join.memoize.flatten
      b <- fibb.join.memoize.flatten

    } yield (a + b)
    x

  }

  it("Joining fiber returns the wrapped effect") {
    val p = for {
      fiber <- ZIO.succeed("a").fork
      r <- fiber.join
    } yield r
    eval(p) shouldBe ("a")
  }
  it("fiber await returns an effect containing an Exit value, which provides full information on how the fiber completed.") {
    // val error = Task.fail(new RuntimeException("some error")).fork
    val exit = for {
      fiber <- zio.IO.succeed("Hello").fork
      exit <- fiber.await
    } yield exit
    eval(exit) shouldBe zio.Exit.Success("Hello")
  }

  it("ZIO timeout returns an Option, in case of timeout, returns None") {
    import zio.duration._
    val p = for {
      zio <- zio.blocking.effectBlocking(Thread.sleep(5000)).timeout(2.seconds)
    } yield zio
    eval(p) shouldBe (None)
  }

  it("test zio repeat") {
    import zio.blocking._
    import zio.duration._
    val print = effectBlocking(println("hello world")).repeat(Schedule.spaced(1.seconds))
    val p = for {
      fiber <- print.fork
      e <- ZIO.sleep(10.seconds) *> fiber.interrupt
    } yield e
    eval(p)
  }

  it("test interrupt") {
    import zio.duration._
    import zio.blocking._
    val blocking = effectBlocking({
      var count = 0
      while (true) {
        Thread.sleep(1000)
        count += 1
        println(s"print output from a fiber:$count")
      }
    })

    val exit = for {
      fiber <- blocking.fork
      _ <- ZIO.sleep(Duration(10, TimeUnit.SECONDS))
      exit <- fiber.interrupt
    } yield exit
    val exitValue = eval(exit)
    println(exitValue)
  }
  it("If a fiber's value is not needed, it could be interrupted,interrupted fiber return a exit vale") {
    val exit = for {
      fiber <- zio.IO.succeed("run forerver").fork
      exit <- fiber.interrupt
    } yield exit
    eval(exit) shouldBe zio.Exit.Failure(Interrupt(Id(34, 0)))
  }

  it("test simple join") {
    val msg = for {
      fiber <- ZIO.succeed("Hi!").fork
      message <- fiber.join
    } yield message
    eval(msg) shouldBe "Hi!"
  }

  it("test fib") {
    val fib100Fiber = fib(10)
    eval(fib100Fiber) shouldBe 55
  }

  it("test race") {
    val raced = fib(10) race fib(100)
    eval(raced) shouldBe 55
  }

  it("fiber compose: zip") {

    val zip = for {
      fiber1 <- ZIO.succeed("fiber1").fork
      fiber2 <- ZIO.succeed("fiber2").fork

      zippedFiber = fiber1.zip(fiber2)
      f <- zippedFiber.join
    } yield f

    eval(zip) shouldBe (("fiber1", "fiber2"))
  }

  it("fiber compose: zipwith, zip let user provide a function to map to results") {
    val zip = for {
      fiber1 <- ZIO.succeed("fiber1").fork
      fiber2 <- ZIO.succeed("fiber2").fork
      zippedFiber = fiber1.zipWith(fiber2)((s1, s2) => s"$s1 zipped with $s2")
      f <- zippedFiber.join
    } yield f

    eval(zip) shouldBe ("fiber1 zipped with fiber2")
  }

  it("ZIO collectAllPar") {
    val f1 = ZIO.succeed("a")
    val f2 = ZIO.succeed("b")
    val f1f2 = ZIO.collectAllPar(List(f1, f2))
    eval(f1f2) shouldBe (List("a", "b"))
  }
  it("ZIO foreachPar") {
    val r:UIO[List[Int]] = ZIO.foreachPar(List(1, 2))(i => ZIO.succeed(i * 2))
    eval(r) shouldBe (List(2, 4))

  }

  it("ZIO foreach") {
    val r:UIO[List[Int]] = ZIO.foreach(List(1, 2))(i => ZIO.succeed(i * 2))
    eval(r) shouldBe (List(2, 4))
  }

  it("reduceAll/reduceAllPar, ParN up to 'N' fiber in parallel, reduce zero is a ZIO effect "){
    val zioList = List(ZIO.succeed(1),ZIO.succeed(2))
    val r:UIO[Int] = ZIO.reduceAll(ZIO.succeed(0),zioList)((a,b) => a+b)
    eval(r) shouldBe (3)
    val rPar:UIO[Int] = ZIO.reduceAllPar(ZIO.succeed(0),zioList)((a,b) => a+b)
    eval(rPar) shouldBe (3)
    /* TODO: not compilable
    val zioList3:List[UIO[Int]] = List(1,2,3,4,5).map(ZIO.succeed(_))
    val rParN:UIO[Int] = ZIO.reduceAllParN(2)(UIO(0),zioList3)((a,b) => a + b)
    eval(rParN) shouldBe (6)
    */
  }
  it("mergeAll/mergeAllPar/mergeAllParN,merge zero is a value type"){
    val zioList3:Iterable[UIO[Int]] = (1 to 3).map(ZIO.succeed(_))
    val mergeAll = ZIO.mergeAll(zioList3)(0)((b, a) => b + a)
    eval(mergeAll) shouldBe (6)

    val mergeAllPar = ZIO.mergeAllPar(zioList3)(0)((b, a) => b + a)
    eval(mergeAllPar) shouldBe (6)

    val mergeAllParN = ZIO.mergeAllParN(2)(zioList3)(0)((b, a) => b + a)
    eval(mergeAllParN) shouldBe (6)

  }

  it("or else") {

    val s = for {
      f1 <- zio.IO.succeed("hello").fork
      f2 <- zio.IO.fail("oops").fork
      f = f2.orElse(f1)
      r <- f.join
    } yield r
    eval(s) shouldBe "hello"
  }


  it("fiberlocal operations, make,set,get,empty") {

    val fiberLocal = for {
      counter <- FiberRef.make[Int](0)
      _ <- counter.set(10)
      x <- counter.get
      _ <- counter.update(x => x + 10)
    } yield x
    eval(fiberLocal) shouldBe Some(20)


  }


}
