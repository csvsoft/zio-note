package zio.notes.create

import org.scalatest.{FunSpec, Matchers}
import scalaz.zio.{DefaultRuntime, ZIO}

abstract class BasicSpec extends FunSpec with Matchers{

  protected def getDefaultRunTime(): DefaultRuntime ={
    new  DefaultRuntime{}
  }

  protected def eval[E,A](zio:ZIO[Any,E,A]):A = getDefaultRunTime().unsafeRun(zio)
}
