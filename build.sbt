import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import xerial.sbt.Sonatype._

licenses                := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
name                    := "sbt-amm"
organization            := "com.github.hirofumi"
publishMavenStyle       := true
publishTo               := sonatypePublishTo.value
sbtPlugin               := true
scriptedBufferLog       := false
scriptedLaunchOpts     ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
sonatypeProjectHosting  := Some(GitHubHosting("hirofumi", "sbt-amm", "hirofummy@gmail.com"))

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard"
)

scalacOptions in (Compile, compile) ++= Seq(
  "-Xfatal-warnings",
  "-Xlint",
  "-Ywarn-unused-import"
)

scalacOptions in (Compile, console) ++= Seq(
  "-Xlint:-unused"
)
