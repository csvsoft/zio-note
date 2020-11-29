package zio.notes.create

import zio.ZIO

class EffectOperationSpec extends BasicSpec {

  /**
   *  ZIP:if either the left or right hand sides fail, then the composed effect will fail,
   *   zipped result will be a tuple
   */
  it("zip: "){
    val zippedZIO = ZIO.succeed("hello") zip ZIO.succeed(2)
    eval(zippedZIO) shouldBe (("hello",2))
  }
  it("zip failed"){
    val zippedZIO = ZIO.succeed("hello") zip ZIO.fail(2)
    eval(zippedZIO.either) shouldBe (Left(2))
  }

  it("zipwith failed"){
    val zippedZIO = ZIO.succeed("hello").zipWith( ZIO.fail(2))((s1:String,s2:String) => s"$s1 and $s2")
    eval(zippedZIO.either) shouldBe (Left(2))
  }
  it("zipRight/zipLeft if one side result is not useful, returns only right or left side value"){
    import zio.console._
    val zipR = putStrLn("How are you doing?").zipRight(ZIO.succeed("Good"))
    eval(zipR) shouldBe "Good"
  }
}
