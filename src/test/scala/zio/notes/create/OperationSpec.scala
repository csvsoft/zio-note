package zio.notes.create

import zio.{UIO, ZIO}

class OperationSpec extends BasicSpec{

  it("zip with combine two result into a tuple"){
    val a = ZIO.succeed("a")
    val b = ZIO.succeed("b")
    val zippedAB:UIO[(String,String)] = a.zip(b)
    eval(zippedAB) shouldBe (("a","b"))
  }
  it("zip right *> will only take the right result"){
    val zipRight = ZIO.succeed("left") *> ZIO.succeed("b")
    val zipRight2 = ZIO.succeed("left").zipRight(ZIO.succeed("right"))
    eval(zipRight) shouldBe ("b")
    eval(zipRight2) shouldBe ("right")
  }

  it("zip left <* will only take the left result"){
    val zipLeft = ZIO.succeed("left") <* ZIO.succeed("b")
    val zipLeft2 = ZIO.succeed("left").zipLeft(ZIO.succeed("right"))
    eval(zipLeft) shouldBe ("left")
    eval(zipLeft2) shouldBe ("left")
  }
  it("collect all collects all results"){
    val list = List(ZIO.succeed(1),ZIO.succeed(2))
    val collected = ZIO.collectAll(list)
    eval(collected) shouldBe (List(1,2))
  }
  it("zipPar"){
    val a = ZIO.succeed("a")
    val b = ZIO.succeed("b")
    val ab = a.zipPar(b)
    eval(ab) shouldBe(("a","b"))

  }
}
