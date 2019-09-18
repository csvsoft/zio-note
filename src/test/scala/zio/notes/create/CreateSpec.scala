package zio.notes.create

import scalaz.zio.{ZIO}

import scala.concurrent.Future
import scala.io.Source

class CreateSpec extends BasicSpec {




  //protected def run[E,A](zio:ZIO[DefaultRuntime,E,A]):A = getDefaultRunTime.unsafeRun(zio)
 // protected def mkEnv[R]:UIO[R] = ZIO.succeed(new Any())
  describe("ZIO creation"){
    it ("Eagerly create a ZIO"){
      val zioEager = ZIO.succeed("hello")
      getDefaultRunTime.unsafeRun(zioEager) shouldBe("hello")

    }
    it ("Lazily create a ZIO"){
      lazy val lazyV = "lazy x"
      val zioLazy = ZIO.succeedLazy(lazyV)
      getDefaultRunTime.unsafeRun(zioLazy) shouldBe(lazyV)

    }
    it("Should be able to create a ZIO from Option"){
      val someInt = Some(2)
      val zioOption = ZIO.fromOption(someInt)
      getDefaultRunTime.unsafeRun(zioOption) shouldBe(2)

      val noneInt = Option.empty[Int]
      val zioNone:ZIO[Any,Unit,Int] = ZIO.fromOption(noneInt)
      val zioNoneWithError:ZIO[Any,Nothing,Either[String,Int]]= zioNone.mapError(_ => "Got none Int").either
      getDefaultRunTime.unsafeRun(zioNoneWithError) shouldBe(Left("Got none Int"))


    }

    it("Should be able to create a ZIO from either"){
      val zeither = ZIO.fromEither(Right("x"))
      getDefaultRunTime.unsafeRun(zeither) shouldBe("x")
    }

    it("Should be able to create a ZIO from future"){
      val zFuture = ZIO.fromFuture(_ => Future.successful("hello"))
      getDefaultRunTime.unsafeRun(zFuture) shouldBe("hello")
    }

    it("Should be able to create a ZIO from synchronous side effect"){
      val zEffect= ZIO.effect(Source.fromFile("file1.txt").getLines()).either
      getDefaultRunTime.unsafeRun(zEffect) match{
        case Left(t) => t shouldBe a [Throwable]
        case Right(_) => "X" shouldBe "Y"
      }
    }
    it("Blocking will evaluate the zio effect on the blocking thread pool"){
      import scalaz.zio.blocking._
       val zBlocking =  effectBlocking({Thread.sleep(2000)
         println(s"${Thread.currentThread().getName}")
       })
      getDefaultRunTime().unsafeRun(zBlocking) shouldBe (())
    }


  }
}
