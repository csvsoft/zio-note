package zio.notes.create

import org.scalatest.{FunSpec, Matchers}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.{Runtime, ZIO}

abstract class BasicSpec extends FunSpec with Matchers{



  protected def eval[E,A](zio:ZIO[Clock with Console with Blocking,E,A]):A = Runtime.default.unsafeRun(zio)
}
