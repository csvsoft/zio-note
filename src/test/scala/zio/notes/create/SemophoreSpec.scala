package zio.notes.create

import java.util.concurrent.TimeUnit

import scalaz.zio.{DefaultRuntime, Semaphore, ZIO}
import scalaz.zio.console._
import scalaz.zio.duration.Duration

class SemophoreSpec extends BasicSpec {

  it("Semaphore is used as permits to synchronize operations between fibers, it has acquire and release operations") {

    val task = for {
      _ <- putStrLn("Starting")
      _ <- ZIO.sleep(Duration(2, TimeUnit.SECONDS))
      _ <- putStrLn("End")
    } yield ()

    val semTask = (sem: Semaphore) => for {
      _ <- sem.acquireN(1)
      _ <- task
      _ <- sem.releaseN(1) // shortcut is sem.release
    } yield ()

    /*
    val semTask2 = (sem: Semaphore) => for {
      _ <- sem.withPermit(task)
    } yield ()
    // semTask1 is equivalent to semTask2, if acquire followed by release operation.
 */
    val semTaskSeq = (sem: Semaphore) => (1 to 3).map(_ => semTask(sem))

    val semOps = for {
      sem <- Semaphore.make(1)
      seq <- ZIO.effectTotal(semTaskSeq(sem))
      res <- ZIO.collectAllPar(seq)
    } yield res
    new DefaultRuntime {}.unsafeRun(semOps)
  }

}
