package zio.notes.create

import java.io.{ByteArrayInputStream, File, FileInputStream, FileNotFoundException, InputStream}

import zio.{IO, Schedule, Task, ZIO}

class ErrorSpec extends BasicSpec {

  private def getNonExistInputStream(): Task[InputStream] = {
    ZIO.effect(new FileInputStream(new File("test.txt")))
  }

  it("Either: takes ZIO[R,E,A] to ZIO[R,Either[E,A]") {
    val zei: IO[String, Int] = ZIO.fromEither({
      val e: Either[String, Int] = Left("error")
      e
    })
    eval(zei.either) shouldBe (Left("error"))
  }
  it("catch all:If you want to catch and recover from all types of errors and effectfully attempt recovery, you can use the catchAll method") {
    val openFileWithBackup: ZIO[Any, Nothing, InputStream] = getNonExistInputStream.catchAll(_ => ZIO.succeed(new ByteArrayInputStream(Array[Byte]())))
    eval(openFileWithBackup).isInstanceOf[InputStream] shouldBe true
  }

  it("fold: handle both error and success with alternative value") {
    val fold = getNonExistInputStream.fold(t => {
      t.getMessage() + "error"
    }, in => {
      in.close(); "success"
    })
    eval(fold).contains("error") shouldBe true
  }
  it("catch some will rethrow errors") {
    val openFileWithBackup: ZIO[Any, Throwable, InputStream] = getNonExistInputStream.catchSome {
      case _: FileNotFoundException => ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
    }
    eval(openFileWithBackup).isInstanceOf[InputStream] shouldBe true

  }

  it("retry") {
    //import scalaz.zio.clock._
    val openFile = getNonExistInputStream()
      .retry(Schedule.recurs(3))
      .catchSome {
        case _: FileNotFoundException => ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
      }
    eval(openFile).isInstanceOf[InputStream] shouldBe true


    // retry or else
    val openFile2 = getNonExistInputStream()
      .retryOrElse(Schedule.recurs(3)
        , (e: Throwable, r: Long) => {
          println(s"Retried:$r")
          println(s"Error:$e")
          ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
        })
    eval(openFile2).isInstanceOf[InputStream] shouldBe true
  }

}
