val Specs2Version     = "4.1.0"
val LogbackVersion    = "1.2.3"
val ScalaLogVersion   = "3.9.2"
val ZioVersion        = "1.0-RC5"
val ScalaTestVersion  = "3.0.5"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    packageName in Docker := "zio-todo",
    dockerUsername in Docker := Some("grumpyraven"),
    dockerExposedPorts in Docker := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq("MIT" -> url(s"https://github.com/mschuwalow/${name.value}/blob/v${version.value}/LICENSE")),
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
      "-Ywarn-unused:_",
      "-Ywarn-inaccessible",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-opt:l:inline"
    ),
    libraryDependencies ++= Seq(

      "org.slf4j"                   %  "slf4j-log4j12"            % "1.7.26",

      "com.typesafe.scala-logging"  %% "scala-logging"            % ScalaLogVersion,

      "org.scalaz"                  %% "scalaz-zio"               % ZioVersion,
      "org.scalaz"                  %% "scalaz-zio-streams"       % ZioVersion,
      "org.scalaz"                  %% "scalaz-zio-interop-cats"  % ZioVersion,

      "org.scalatest"               %% "scalatest"                % ScalaTestVersion % "test",

      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4")
    )
  )

libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.0-RC12-1"


//release
import ReleaseTransformations._
import ReleasePlugin.autoImport._
import sbtrelease.{Git, Utilities}
import Utilities._

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  pushChanges,
  tagRelease,
  mergeReleaseVersion,
  ReleaseStep(releaseStepTask(publish in Docker)),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

val mergeBranch = "master"

val mergeReleaseVersion = ReleaseStep(action = st => {
  val git = st.extract.get(releaseVcs).get.asInstanceOf[Git]
  val curBranch = (git.cmd("rev-parse", "--abbrev-ref", "HEAD") !!).trim
  st.log.info(s"####### current branch: $curBranch")
  git.cmd("checkout", mergeBranch) ! st.log
  st.log.info(s"####### pull $mergeBranch")
  git.cmd("pull") ! st.log
  st.log.info(s"####### merge")
  git.cmd("merge", curBranch, "--no-ff", "--no-edit") ! st.log
  st.log.info(s"####### push")
  git.cmd("push", "origin", s"$mergeBranch:$mergeBranch") ! st.log
  st.log.info(s"####### checkout $curBranch")
  git.cmd("checkout", curBranch) ! st.log
  st
})
