package zio.notes.create

import zio.{ Promise, ZIO, ZManaged, console}
import zio.console._

class ZManagedSpec extends BasicSpec {

  it("ZManaged"){
    val zM :ZManaged[Console, Nothing, Unit]  = ZManaged.make(console.putStrLn("acquiring"))(_ => console.putStrLn("releasing"))
     val used = zM.use(_=>console.putStrLn("using"))
    eval(used)
  }

  it("Promise is a variable that can only be set once"){
    val p = Promise.make[Exception,String]
    val psuccess = p.flatMap( x => x.succeed("promise is successfully set"))
    val r = eval(psuccess)
    r shouldBe true

    val p2 = Promise.make[Exception,String]
    val psuccess2 = p2.flatMap( x => x.fail(new Exception("Unable to set")))
    val r2= eval(psuccess2)
    r2 shouldBe true // result boolean represent whether the operation took place successfully

  }

  it("Simple example of promise"){
    import zio.duration._
    import zio.clock._
    import scala.language.postfixOps

    val example = for{
      promise <-  Promise.make[Exception, String]
      taskSend    = (ZIO.succeed("Hello world") <* sleep(1 second)).flatMap(promise.succeed) // set promise
      taskReceive = promise.await.flatMap(s=>console.putStrLn(s)) // wait for the promise to be set and print the result
      fiberSend   <- taskSend.fork
      fiberReceive <- taskReceive.fork
      r <- (fiberSend zip fiberReceive).join

    } yield r
    eval(example) shouldBe ((true,()))
  }

}
