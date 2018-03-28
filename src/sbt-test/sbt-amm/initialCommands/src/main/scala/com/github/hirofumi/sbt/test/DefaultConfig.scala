package com.github.hirofumi.sbt.test

import java.nio.file.{Files, Path, Paths}
import scala.util.Properties

object DefaultConfig {

  def run(): Path =
    Files.write(Paths.get("target", "DefaultConfig"), Properties.versionNumberString.getBytes)

}
