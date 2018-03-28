package com.github.hirofumi.sbt.test

import java.nio.file.{Files, Path, Paths}
import scala.util.Properties

object TestConfig {

  def run(): Path =
    Files.write(Paths.get("target", "TestConfig"), Properties.versionNumberString.getBytes)

}
