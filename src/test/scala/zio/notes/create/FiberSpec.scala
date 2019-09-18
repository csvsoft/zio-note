package zio.notes.create

import scalaz.zio.{FiberLocal, UIO, ZIO}

class FiberSpec extends BasicSpec {


  def fib(n: Long): UIO[Long] = {
    val x :UIO[Long] = if (n <= 2) UIO.succeedLazy(1)
    else for {
      fiba <- fib(n-1).fork
      fibb <- fib(n-2).fork

      a <- fiba.join.memoize.flatten
      b <- fibb.join.memoize.flatten

    }  yield (a + b)
    x

  }

  it("test simple join"){
    val msg = for {
      fiber   <- ZIO.succeed("Hi!").fork
      message <- fiber.join
    } yield message
    eval(msg) shouldBe "Hi!"
  }

  it("test fib"){
    val fib100Fiber = fib(10)
    eval(fib100Fiber) shouldBe 55
  }

  it("test race"){
    val  raced  = fib(10) race fib(100)
    eval(raced) shouldBe 55
  }

  it ("fiberlocal operations, make,set,get,empty"){

     val fiberLocal = for{
     counter <- FiberLocal.make[Int]
      _ <- counter.set(10)
      x  <- counter.get
       _ <- counter.empty
    } yield x
    eval(fiberLocal) shouldBe Some(10)

    // use locally
     val locally = for{
        local <- FiberLocal.make[Int]
        fiber <- local.locally(10)(local.get.map(optInt => optInt.map(i=>i.toString + "x"))).fork
        r <- fiber.join
     }yield r

    eval(locally) shouldBe Some("10x")
  }




}
