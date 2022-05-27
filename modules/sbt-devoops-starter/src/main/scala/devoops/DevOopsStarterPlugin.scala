package devoops

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys.{name, scalaVersion, version}
import sbt._
import Def.Setting
import sbtwelcome.WelcomePlugin
import sbtwelcome.WelcomePlugin.autoImport.{logo, logoColor, usefulTasks}
import scalafix.sbt.ScalafixPlugin

import scala.{Console => sConsole}

/** @author Kevin Lee
  * @since 2022-05-21
  */
object DevOopsStarterPlugin extends AutoPlugin {

  override def requires: Plugins =
    DevOopsScalaPlugin && DevOopsSbtExtraPlugin && ScalafmtPlugin && ScalafixPlugin && WelcomePlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val DefaultLogo: String =
      """
        |       __   __      ___           ____
        |  ___ / /  / /_____/ _ \___ _  __/ __ \___  ___  ___
        | (_-</ _ \/ __/___/ // / -_) |/ / /_/ / _ \/ _ \(_-<
        |/___/_.__/\__/   /____/\__/|___/\____/\___/ .__/___/
        |                                         /_/
        |""".stripMargin

    lazy val theLogo: SettingKey[String] = settingKey(
      "The logo String used in the settingKey logo (default: The value of DefaultLogo)"
    )

    lazy val logoAdditionalInfo: SettingKey[String] = settingKey(
      """The additional information displayed after logo (default: "")"""
    )

    def genLogo(
      projectName: String,
      projectVersion: String,
      scalaVersion: String,
      theLogo: String,
      logoAdditionalInfo: String
    ): String = {
      raw"""$theLogo
           |${sConsole.BLUE}$projectName${sConsole.RESET} ${sConsole.GREEN}$projectVersion${sConsole.RESET}
           |${sConsole.YELLOW}Scala $scalaVersion${sConsole.RESET}
           |-----------------------------------------------------
           |$logoAdditionalInfo""".stripMargin
    }

  }

  import autoImport._
  import sbtwelcome._

  override def projectSettings: Seq[Setting[_]] = Seq(
    theLogo            := DefaultLogo,
    logoAdditionalInfo := "",
    logo               := genLogo(
      name.value,
      version.value,
      scalaVersion.value,
      theLogo.value,
      logoAdditionalInfo.value
    ),
    usefulTasks        := Seq(
      UsefulTask("r", "reload", "Run reload"),
      UsefulTask("cln", "clean", "Run clean"),
      UsefulTask("c", "compile", "Run compile"),
      UsefulTask("cc", "+compile", "Run cross-scalaVersion compile"),
      UsefulTask("tc", "Test/compile", "Run Test/compile"),
      UsefulTask("ctc", "+Test/compile", "Run cross-scalaVersion Test/compile"),
      UsefulTask("t", "test", "Run test"),
      UsefulTask("ct", "+test", "Run cross-scalaVersion test"),
      UsefulTask("fmtchk", "scalafmtCheckAll", "Run scalafmtCheckAll"),
      UsefulTask("fmt", "scalafmtAll", "Run scalafmtAll"),
      UsefulTask("fixchk", "scalafixAll --check", "Run scalafixAll --check"),
      UsefulTask("fix", "scalafixAll", "Run scalafixAll"),
      UsefulTask("chk", "fmtchk; fixchk", "Run scalafmtCheckAll; scalafixAll --check"),
      UsefulTask("pl", "publishLocal", "Run publishLocal"),
    ),
    logoColor          := sConsole.MAGENTA
  )

}
