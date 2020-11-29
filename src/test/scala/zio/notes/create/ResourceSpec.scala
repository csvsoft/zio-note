package zio.notes.create

import java.io.FileInputStream
import zio.{FiberFailure, UIO, ZIO}

class ResourceSpec extends BasicSpec {

  it("ensuring:provides similar functionality to try / finally with the ZIO#ensuring method.") {
    val finalizer = ZIO.effectTotal(println("Finalizer invoked"))
    val zioWithFinalizer = ZIO.fail("failed").ensuring(finalizer)
    val caught = intercept[FiberFailure] {
      eval(zioWithFinalizer)
    }
    caught.getMessage should include("fail")
  }

  it("bracket") {
    val in = new FileInputStream("src/test/resources/log4j.properties")
    val b =  zio.IO.effect(in).bracket(in => UIO.effectTotal(in.close())) { in =>
      val printlns = scala.io.Source.fromInputStream(in, "UTF-8").getLines().foreach(println)
      zio.IO.effect(printlns)
    }
    eval(b)

  }

}
