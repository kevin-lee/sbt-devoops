package devoops

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys._
import sbt._
import Def.Setting
import cats.effect.{Async, IO}
import devoops.data.{CommonKeys, DevOopsLogLevel}
import devoops.types.StarterError
import effectie.core.Fx
import effectie.syntax.all._
import extras.cats.syntax.all._
import extras.scala.io.Color
import kevinlee.github.GitHubApi
import kevinlee.github.data.GitHub
import kevinlee.http.HttpClient
import loggerf.core.{Log => LogF}
import loggerf.logger.{CanLog, SbtLogger}
import org.http4s.blaze.client.BlazeClientBuilder
import sbt.{IO => SbtIO}
import sbtwelcome.WelcomePlugin
import sbtwelcome.WelcomePlugin.autoImport.{logo, logoColor, usefulTasks}
import scalafix.sbt.ScalafixPlugin
import extras.scala.io.syntax.color._

import java.nio.charset.StandardCharsets

/** @author Kevin Lee
  * @since 2022-05-21
  */
object DevOopsStarterPlugin extends AutoPlugin {

  override def requires: Plugins =
    DevOopsScalaPlugin && DevOopsSbtExtraPlugin && ScalafmtPlugin && ScalafixPlugin && WelcomePlugin

  override def trigger: PluginTrigger = allRequirements

  object autoImport extends CommonKeys {

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

    lazy val starterWriteDefaultScalafmtConf: TaskKey[Unit] = taskKey("Write the default .scalafmt.conf")

    def genLogo(
      projectName: String,
      projectVersion: String,
      scalaVersion: String,
      theLogo: String,
      logoAdditionalInfo: String
    ): String = {
      raw"""$theLogo
           |${projectName.blue} ${projectVersion.green}
           |${s"Scala $scalaVersion".colored(Color.yellow)}
           |-----------------------------------------------------
           |$logoAdditionalInfo""".stripMargin
    }

  }

  import autoImport._
  import sbtwelcome._

  override def buildSettings: Seq[Def.Setting[_]] = Seq(
    devOopsLogLevel                 := DevOopsLogLevel.info.render,
    starterWriteDefaultScalafmtConf := {
      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      val dialectVersion = s"scala${scalaBinaryVersion.value.replace(".", "")}"
      val baseDirFile    = (ThisBuild / baseDirectory).value
      val outFile        = baseDirFile / ".scalafmt.conf"

      import effectie.cats.fx.ioFx
      import loggerf.cats.instances._

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      import cats.effect.unsafe.implicits.global

      if (outFile.exists()) {
        log.info(
          raw"""${outFile.getName.blue} already exists so it will not rewrite ${outFile.getName.blue}.
               |If you want to regenerate it, please remove the existing one.
               |""".stripMargin
        )
      } else {
        writeDefaultScalafmtConf[IO](dialectVersion, outFile)
          .unsafeRunSync() match {
          case Left(err) => sys.error(err.render)
          case Right(_) => log.info(s"The default ${outFile.getName.blue} file has been written.")
        }
      }

    }
  )

  def writeDefaultScalafmtConf[F[_]: Fx: LogF: Async](dialectVersion: String, outFile: File)(
    implicit LV: DevOopsLogLevel
  ): F[Either[StarterError, Unit]] =
    BlazeClientBuilder[F]
      .resource
      .use { client =>
        (for {
          gitHubApi <- GitHubApi[F](HttpClient[F](client)).rightTF
          tags <- gitHubApi
                    .getTags(
                      GitHub.GitHubRepoWithAuth(
                        GitHub.Repo(GitHub.Repo.Org("scalameta"), GitHub.Repo.Name("scalafmt")),
                        none
                      )
                    )
                    .t
                    .leftMap(StarterError.gitHub("getting the tags", _))
          theLatestScalafmtVersion = tags.headOption.fold("3.5.4")(_.name.name.stripPrefix("v"))
          scalafmtConfTemplate <- effectOf[F](
                                    SbtIO.readStream(
                                      this.getClass.getResourceAsStream("/scalafmt/default-scalafmt.conf.template"),
                                      StandardCharsets.UTF_8
                                    )
                                  ).catchNonFatal { err =>
                                    StarterError.resourceReadWrite(
                                      s"reading the default .scalafmt.conf template file at resources/scalafmt/default-scalafmt.conf.template",
                                      err.toString
                                    )

                                  }.t
          scalafmtConf         <- effectOf[F](
                                    scalafmtConfTemplate
                                      .replace("%SCALAFMT_VERSION%", theLatestScalafmtVersion)
                                      .replace("%DIALECT_VERSION%", dialectVersion)
                                  ).rightT[StarterError]
          _                    <- effectOf[F](
                                    SbtIO.write(outFile, scalafmtConf, StandardCharsets.UTF_8)
                                  ).catchNonFatal { err =>
                                    StarterError.resourceReadWrite(
                                      s"writing the default .scalafmt.conf to ${outFile.toString}",
                                      err.toString
                                    )
                                  }.t
        } yield ()).value
      }

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
    logoColor          := Color.magenta.toAnsi
  )

}
