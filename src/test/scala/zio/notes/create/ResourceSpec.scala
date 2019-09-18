package zio.notes.create

import scalaz.zio.{FiberFailure, ZIO}

class ResourceSpec extends BasicSpec {

  it("ensuring"){
    val finalizer = ZIO.effectTotal(println("Finalizing"))
    val zioWithFinalizer = ZIO.fail("failed").ensuring(finalizer)
    val caught = intercept[FiberFailure] {
      eval(zioWithFinalizer)
    }
    caught.getMessage should include ("fail")
  }

}
