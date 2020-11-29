val Specs2Version     = "4.1.0"
val LogbackVersion    = "1.2.3"
val ScalaLogVersion   = "3.9.2"
val ZioVersion        = "1.0.3"
val ScalaTestVersion  = "3.0.5"

lazy val root = (project in file("."))
  //.enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    scalaVersion := "2.12.8",
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-Xfuture",
      "-encoding", "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Ypartial-unification",
      "-Xfatal-warnings",
      "-Xlint:-infer-any,_",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      //"-Ywarn-unused:_",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-opt:l:inline"
    ),
    libraryDependencies ++= Seq(

      "org.slf4j"                   %  "slf4j-log4j12"            % "1.7.26",

      "com.typesafe.scala-logging"  %% "scala-logging"            % ScalaLogVersion,

     // "org.scalaz"                  %% "scalaz-zio"               % ZioVersion,
     // "org.scalaz"                  %% "scalaz-zio-streams"       % ZioVersion,
     // "org.scalaz"                  %% "scalaz-zio-interop-cats"  % ZioVersion,


      // https://mvnrepository.com/artifact/dev.zio/zio
       "dev.zio" %% "zio" % ZioVersion,
       "dev.zio" %% "zio-streams" % ZioVersion,
       "dev.zio" %% "zio-test" % ZioVersion % Test,




      "org.scalatest"               %% "scalatest"                % ScalaTestVersion % "test",

      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")
    )
  )

//libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC12-1"
// https://mvnrepository.com/artifact/dev.zio/zio-nio
libraryDependencies += "dev.zio" %% "zio-nio" % "1.0.0-RC10"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio-test"          % ZioVersion % "test",
  "dev.zio" %% "zio-test-sbt"      % ZioVersion % "test",
  "dev.zio" %% "zio-test-magnolia" % ZioVersion % "test" // optional
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")