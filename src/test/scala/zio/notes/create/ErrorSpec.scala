package zio.notes.create

import java.io.{ByteArrayInputStream, File, FileInputStream, FileNotFoundException, InputStream}

import scalaz.zio.{Schedule, Task, ZIO}

class ErrorSpec extends BasicSpec {

  private def getNonExistInputStream():Task[InputStream]={
    ZIO.effect(new FileInputStream(new File("test.txt")))
  }
  it("catch all"){

    val openFileWithBackup :ZIO[Any,Nothing,InputStream] = getNonExistInputStream.catchAll(_ => ZIO.succeed(new ByteArrayInputStream(Array[Byte]())))
    eval(openFileWithBackup).isInstanceOf[InputStream] shouldBe  true
  }

  it("catch some will rethrow errors"){
    val openFileWithBackup :ZIO[Any,Throwable,InputStream] = getNonExistInputStream.catchSome{
      case _:FileNotFoundException => ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
    }
    eval(openFileWithBackup).isInstanceOf[InputStream] shouldBe  true
  }

  it( "retry"){
    //import scalaz.zio.clock._
    val openFile= getNonExistInputStream()
                   .retry(Schedule.recurs(3))
      .catchSome{
        case _:FileNotFoundException => ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
      }
    getDefaultRunTime.unsafeRun(openFile).isInstanceOf[InputStream] shouldBe  true


    // retry or else
    val openFile2 = getNonExistInputStream()
      .retryOrElse( Schedule.recurs(3)
         ,(e:Any,r:Int) =>{
          println(s"Retried:$r")
          println(s"Error:$e")
          ZIO.succeed(new ByteArrayInputStream(Array[Byte]()))
        })
    getDefaultRunTime.unsafeRun(openFile2).isInstanceOf[InputStream] shouldBe  true


  }

  /*
   */

}
