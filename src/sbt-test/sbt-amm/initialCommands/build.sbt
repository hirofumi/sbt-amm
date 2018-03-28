ammCode := Some("DefaultConfig.run()")
initialCommands in console := "import com.github.hirofumi.sbt.test.DefaultConfig"

ammCode in Test := Some("TestConfig.run()")
initialCommands in (Test, console) := "import com.github.hirofumi.sbt.test.TestConfig"
