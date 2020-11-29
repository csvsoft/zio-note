package zio.notes.create


class ParseArgTest extends BasicSpec {

  case class Args(configFile: String = "", overrided: Boolean = false)

  val usage = "Usage: -c --config <configFile> -o --override <true|false>"

  def parseArg(argList: List[String], currentArg: Args = Args()): Either[String, Args] = argList match {
    case ("--config" | "-c") :: c :: tail => parseArg(tail, currentArg.copy(configFile = c))
    case ("-o" | "--override") :: o :: tail if o.toUpperCase() == "TRUE" || o.toUpperCase() == "FALSE"
    => parseArg(tail, currentArg.copy(overrided = if (o.toUpperCase() == "TRUE") true else false))
    case Nil => Right(currentArg)
    case _ => Left(usage)
  }

  it("Test args parsing") {
    val args = List("--config", "configFile", "-o", "true")
    val parsedArg = parseArg(args)
    parsedArg shouldBe Right(Args("configFile", true))

    val args2 = List("-o", "x", "-c", "configFile")
    val parsedArg2 = parseArg(args2)
    parsedArg2 shouldBe Left(usage)
  }

}
