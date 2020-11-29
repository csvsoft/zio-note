package zio.notes.datatypes

import zio.Chunk
import zio.notes.create.BasicSpec

class ChunkSpec extends BasicSpec{

  it("Chunk is a zio version of array"){
    val chunk = Chunk.fill(10)(1)
    chunk.toSeq.size shouldBe(10)

  }

  it("chunk concatenation"){
    val chunkA = Chunk(1,2)
    val chunkB = Chunk(3,4)
    chunkA ++ chunkB shouldBe(Chunk(1,2,3,4))
  }

  it("Chunks collect: filter and map"){
    val collectChunk = Chunk[Any] ("a", 1.5, "b", 2.0, "c", 2.5)

   // val chunks = Chunk(1.0,2.0,"a","b")
    val textChunks = collectChunk.collect{case x:String => x}
    textChunks shouldBe Chunk("a","b","c")

    val doubleChunk = collectChunk.collect{case d:Double => d}
    doubleChunk shouldBe Chunk(1.5,2.0,2.5)
  }

  it("Chunk collectWhile: collect until predict returns false"){
    val chunk = Chunk(2,3,1,6,8)
    val chunkLessThan3 = chunk.collectWhile{case x:Int if x <= 3 => x}
    chunkLessThan3 shouldBe Chunk(2,3,1)
  }

  it("Drop n elements and drop while"){
    val chunk = Chunk(1,3,4)
    chunk.drop(1) shouldBe Chunk(3,4)
    chunk.dropRight(2) shouldBe Chunk(1)
    Chunk(4,3,8,2).dropWhile(i => i > 2) shouldBe Chunk(2)
  }

  it("zip"){
    Chunk(1,2).zip(Chunk(3,4)) shouldBe Chunk((1,3),(2,4))
  }
  it("zipWith: zip + mapping"){
    Chunk(1,2).zipWith(Chunk("a","b","c"))((i,s) => (i,s)) shouldBe Chunk((1,"a"),(2,"b"))
  }

  it("zipAllWith: provide default values for shorter chunks"){
    //
    Chunk(1,2).zipAllWith(Chunk(3,4,8))( _=> 1, _=> 2)((a:Int,b:Int) => a * b) shouldBe Chunk(3,8,2)
  }

}
