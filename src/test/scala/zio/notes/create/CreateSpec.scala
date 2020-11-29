package zio.notes.create

//import zio.duration.Duration
import zio.{Task, ZIO}

import scala.concurrent.Future
import scala.io.Source

class CreateSpec extends BasicSpec {

  describe("ZIO creation"){
    it ("Eagerly create a ZIO"){
      val zioEager = ZIO.succeed("hello")
      eval(zioEager) shouldBe("hello")

    }
    it ("Lazily create a ZIO"){
      lazy val lazyV = "lazy x"
      val zioLazy = ZIO.succeed(lazyV)
      eval(zioLazy) shouldBe(lazyV)

    }
    it("Should be able to create a ZIO from Option"){
      val someInt = Some(2)
      val zioOption = ZIO.fromOption(someInt)
      eval(zioOption) shouldBe(2)

      val noneInt = Option.empty[Int]
      val zioNone = ZIO.fromOption(noneInt)
      val zioNoneWithError:ZIO[Any,Nothing,Either[String,Int]]= zioNone.mapError(_ => "Got none Int").either
      eval(zioNoneWithError) shouldBe(Left("Got none Int"))


    }

    it("Should be able to create a ZIO from either"){

      val zeither = ZIO.fromEither({val x:Either[Int,String] = Right("x");x})
      eval(zeither) shouldBe("x")
    }

    it("Should be able to create a ZIO from future, future is close to task that throws throwable"){
      val zFuture:Task[String] = ZIO.fromFuture(_ => Future.successful("hello"))
      eval(zFuture) shouldBe("hello")
    }

    it("Should be able to create a ZIO from synchronous side effect"){
      val zEffect= ZIO.effect(Source.fromFile("file1.txt").getLines()).either
      eval(zEffect) match{
        case Left(t) => t shouldBe a [Throwable]
        case Right(_) => "X" shouldBe "Y"
      }
    }
    it("Blocking will evaluate the zio effect on the blocking thread pool"){
      import zio.blocking._
       val zBlocking =  effectBlocking({Thread.sleep(2000)
         println(s"${Thread.currentThread().getName}")
       })
      eval(zBlocking) shouldBe (())
    }

    /*import zio.clock._
    import zio.duration._
    def delay[R,E,A](zio:ZIO[R,E,A])(duration: Duration):ZIO [R with Clock, E, A] ={
      clock.sleep(duration) *> zio
    }
    */

  }
}
