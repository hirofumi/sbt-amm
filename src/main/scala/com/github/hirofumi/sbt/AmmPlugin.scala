package com.github.hirofumi.sbt

import sbt._
import sbt.Keys._
import scala.collection.mutable

object AmmPlugin extends AutoPlugin {

  object autoImport {
    lazy val Amm                     = config("amm")
    lazy val AmmTest                 = config("amm-test")
    lazy val amm                     = inputKey[Unit]("amm")
    lazy val ammBanner               = settingKey[Option[String]]("--banner option of Ammonite")
    lazy val ammCode                 = settingKey[Option[String]]("--code option of Ammonite")
    lazy val ammColor                = settingKey[Option[Boolean]]("--color option of Ammonite")
    lazy val ammHome                 = settingKey[Option[String]]("--home option of Ammonite")
    lazy val ammMainClass            = settingKey[String]("main class which runs Ammonite")
    lazy val ammNoRemoteLogging      = settingKey[Boolean]("--no-remote-logging option of Ammonite")
    lazy val ammPredefCode           = settingKey[Option[String]]("--predef-code option of Ammonite")
    lazy val ammSilent               = settingKey[Boolean]("--silent option of Ammonite")
    lazy val ammSourceCommandSupport = settingKey[Boolean]("support for source command http://ammonite.io/#source")
    lazy val ammVersion              = settingKey[String]("version of Ammonite")
  }
  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] =
    Seq(
      aggregate in amm        := false,
      amm                     := (amm in Compile).evaluated,
      ammBanner               := None,
      ammCode                 := None,
      ammColor                := None,
      ammHome                 := None,
      ammMainClass            := "ammonite.Wrapped",
      ammNoRemoteLogging      := false,
      ammPredefCode           := None,
      ammSilent               := false,
      ammSourceCommandSupport := false,
      ammVersion              := (if (scalaBinaryVersion.value == "2.10") "1.0.3" else "1.1.0")
    ) ++ ammSettings(Compile, Amm) ++ ammSettings(Test, AmmTest)

  override def trigger: PluginTrigger =
    allRequirements

  private[this] def ammSettings(conf: Configuration, ammConf: Configuration): Seq[Setting[_]] =
    inConfig(conf)(
      Seq(
        amm           := (amm in amm).evaluated,
        ammColor      := (if ((fork in amm).value) Some(true) else None),
        ammPredefCode := Option((initialCommands in console).value).filter(_.nonEmpty),
      ) ++ inTask(amm)(
        Seq(
          connectInput   := true,
          forkOptions    := Defaults.forkOptionsTask.value,
          outputStrategy := (if (fork.value) Some(OutputStrategy.StdoutOutput) else outputStrategy.value),
          runner         := Defaults.runnerInit.value
        ) ++ Seq(
          amm := {
            import Def.parserToInput
            val parser = Def.spaceDelimited()
            Def.inputTask {
              val args = mutable.Buffer.empty[String]
              args ++= parser.parsed
              if (args.isEmpty) {
                ammBanner.value.foreach(args ++= Seq("--banner", _))
                ammCode.value.foreach(args ++= Seq("--code", _))
                ammColor.value.map(_.toString).foreach(args ++= Seq("--color", _))
                ammHome.value.foreach(args ++= Seq("--home", _))
                if (ammNoRemoteLogging.value) args += "--no-remote-logging"
                ammPredefCode.value.foreach(args ++= Seq("--predef-code", _))
                if (ammSilent.value) args += "--silent"
              }
              runner.value
                .run(
                  ammMainClass.value,
                  Attributed.data(Classpaths.concatDistinct(fullClasspath, fullClasspath in ammConf).value),
                  args,
                  streams.value.log
                )
                .failed
                .foreach(e => sys.error(e.getMessage))
            }
          }.evaluated
        )
      )
    ) ++ inConfig(ammConf)(
      Defaults.configSettings ++ Classpaths.ivyBaseSettings ++ Seq(
        fullClasspath ++=
          Def.taskDyn({
            if ((ammSourceCommandSupport in (conf, amm)).value) {
              Def.task {
                updateClassifiers.value
                  .configurations
                  .filter(_.configuration.name == conf.name)
                  .flatMap(_.modules)
                  .flatMap(_.artifacts)
                  .filter(_._1.classifier.contains("sources"))
                  .map(_._2)
              }
            } else {
              Def.task(Vector.empty[File])
            }
          }).value,
        libraryDependencies :=
          Seq(
            "com.lihaoyi" % "ammonite" % (ammVersion in (conf, amm)).value cross CrossVersion.full
          ),
        sourceGenerators +=
          Def.task({
            val file = sourceManaged.value / "amm.scala"
            IO.write(
              file,
              """package ammonite
                |object Wrapped extends App {
                |  ammonite.main.ProxyFromEnv.setPropProxyFromEnv()
                |  Main.main0(args.toList, System.in, System.out, System.err)
                |}""".stripMargin
            )
            Seq(file)
          }).taskValue
      )
    )

}
