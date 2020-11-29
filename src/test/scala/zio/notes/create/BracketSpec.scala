package zio.notes.create

import java.io.{FileInputStream, FileOutputStream}
import java.nio.charset.StandardCharsets

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import zio.{URIO, ZIO}

class BracketSpec extends BasicSpec {

  it("bracket") {
    val p = zio.blocking.effectBlocking(new FileInputStream("src/test/resources/test1.txt")).bracket(in => URIO(in.close())) { in => {
      zio.blocking.effectBlocking({
        val bytes = new Array[Byte](400)
        var count = 0
        val outStream = new ByteOutputStream()
        while ( {
          count = in.read(bytes); count
        } != -1) {
          outStream.write(bytes, 0, count)
        }
        outStream.flush()
        val outBytes = outStream.getBytes
        outStream.close
        outBytes
      }).map(bytes => new String(bytes, StandardCharsets.UTF_8))

    }

    }
    eval(p) shouldBe ("Hello")
  }

  it("Managed is an reification of bracket: it can be composed to combine multiple resources") {

    val inFile = zio.Managed.make(zio.blocking.effectBlocking(new FileInputStream("src/test/resources/test1.txt")))(in => ZIO.effectTotal(in.close))
    val outFile = zio.Managed.make(zio.blocking.effectBlocking(new FileOutputStream("src/test/resources/testOut.txt")))(in => ZIO.effectTotal(in.close))

    // combine multiple resources
    val inOut = for {
      in <- inFile
      out <- outFile
    } yield (in, out)

    val p = inOut.use { case (in, out) => {
      zio.blocking.effectBlocking(IOUtils.copyStream(in, out))
    }

    }
    eval(p)
  }
}
