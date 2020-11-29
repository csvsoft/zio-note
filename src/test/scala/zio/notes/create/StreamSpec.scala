package zio.notes.create

import java.nio.file.{Files, Paths}

import zio.{Schedule, ZIO}
import zio.console._
import zio.stream._
//import zio.nio.core.charset.Charset

class StreamSpec extends BasicSpec {

  it("Constructing from  elements"){
    val p = ZStream(1,2,3).runLast
    eval(p) shouldBe Some(3)
  }
  it("Constructing by repeating"){
    val p = ZStream.repeatWith("a",Schedule.recurs(5)).runCollect
    eval(p).size shouldBe 6

  }

  /*
  A Stream[E,A] represent an effectful stream that can produce values of type A, or fails with a
  a value of E
   */
  it("create stream") {
    val a: Stream[Nothing, Int] = zio.stream.Stream(1, 2, 3)
    // val b:Stream[Nothing,String] = Stream.fromIterable(List("a","b","c"))
    val prog = a.map(i => i * 2).foreach(x => zio.console.putStrLn(s"$x"))
    eval(prog)
  }

  it("construct stream from iterable") {
    val sFromIterable = zio.stream.Stream.fromIterable(1 to 5)
    val p = sFromIterable.foreach(x => zio.console.putStrLn(s"$x"))
    eval(p)
  }
  it("construct from inputstream") {
    //val utf8Transducer  = Charset.Standard.utf8.newDecoder.transducer()
    val p = ZStream
      .fromInputStream(Files.newInputStream(Paths.get("src/test/resources/test1.txt")), 1)
      .transduce(ZTransducer.utf8Decode)
      .transduce(ZTransducer.splitLines)
      .foreach(line => putStrLn(line))
    //.foreachChunk(chars => putStrLn(chars.mkString))
    eval(p)
  }
  it("construct from effect"){
    val p = ZStream.fromEffect(ZIO.effect("a"))
      .foreach(line => putStrLn(line))
    //.foreachChunk(chars => putStrLn(chars.mkString))
    eval(p)
  }
  it("construct from bracket"){
    val p = ZStream.bracket(
                            zio.blocking.effectBlocking(Files.newInputStream(Paths.get("src/test/resources/test1.txt")))
                            )(is => zio.blocking.effectBlocking(is.close).orDie)
        .map(in => IOUtils.getText(in))
        .runHead
    eval(p) shouldBe Some("Hello\nworld")
  }

  it("two streams: merge, order is not guranteed"){
    val m = ZStream(1,2,3).merge(ZStream(4,5,6)).runCollect
    val r = eval(m)
   // r.toSeq.toList shouldBe(List(4,5,6,1,2,3))
    r.size shouldBe 6
    //r.head shouldBe 4
    //r.last shouldBe 3

  }

  it("two streams: zip"){
    val z = ZStream(1,2,3).zip(ZStream(4,5,6)).runCollect
    val r = eval(z)
    r.head shouldBe Tuple2(1,4)
  }

  it("two streams: zipWith, zip and map"){
    val z= ZStream(1,2,3).zipWith(ZStream(4,5,6))((a,b) => a + b).runCollect
    val r = eval(z)
    r.head shouldBe (1+4)
  }



  it("mapParN") {
    val p = ZStream.fromIterable(1 to 10)

      .mapMPar(2)(n => zio.ZIO.succeed(n * 2))
      .map(x => x + 10)
        .foreach(n=> putStrLn(n.toString))
    eval(p)
  }


}
