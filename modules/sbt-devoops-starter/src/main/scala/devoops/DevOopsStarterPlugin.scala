package devoops

import org.scalafmt.sbt.ScalafmtPlugin
import sbt.Keys.*
import sbt.{none as *, some as *, *}
import Def.Setting
import cats.effect.{Async, IO}
import cats.syntax.all.*
import devoops.data.{CommonKeys, DevOopsLogLevel}
import devoops.types.StarterError
import effectie.instances.ce3.fx.ioFx
import effectie.core.Fx
import effectie.syntax.all.*
import extras.cats.syntax.all.*
import extras.scala.io.Color
import extras.scala.io.syntax.color.*
import fs2.io.file.Files
import fs2.io.net.Network
import kevinlee.github.GitHubApi
import kevinlee.github.data.GitHub
import kevinlee.http.HttpClient
import kevinlee.sbt.SbtCommon.*

import loggerf.core.Log as LogF
import loggerf.logger.{CanLog, SbtLogger}
import org.http4s.ember.client.EmberClientBuilder
import sbt.IO as SbtIO
import sbtwelcome.WelcomePlugin
import sbtwelcome.WelcomePlugin.autoImport.{aliasColor, logo, logoColor, usefulTasks}
import scalafix.sbt.ScalafixPlugin

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

    lazy val starterWriteDefaultScalafmtConf: TaskKey[Unit] = taskKey(s"Write the (default: ${".scalafmt.conf".blue})")

    lazy val starterWriteDefaultScalafixConf: TaskKey[Unit] = taskKey(
      s"Write the (default ${".scalafix.conf".blue} if the project has only scala 2 or only scala 3. " +
        s"Otherwise ${".scalafix-scala2.conf".blue} and ${".scalafix-scala3.conf".blue})"
    )

    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    def preprocessLogo(logo: String): String =
      if (java.time.LocalDate.now().getMonth == java.time.Month.JUNE) {
        val logoLines = logo.split("\n")
        val maxLength = logoLines.map(_.length).max
        val lines     = logoLines.map { line =>
          val lineLength = line.length
          if (lineLength === 0) {
            line
          } else {
            val additionalSpace =
              if (lineLength < maxLength)
                " " * (maxLength - lineLength)
              else
                ""
            line + additionalSpace
          }
        }
        import extras.scala.io.syntax.truecolor.rainbow.*
        lines
          .map { line =>
            if (line.nonEmpty) line.rainbowed else line
          }
          .mkString("\n")
      } else {
        logo
      }

    def genLogo(
      projectName: String,
      projectVersion: String,
      scalaVersion: String,
      theLogo: String,
      logoAdditionalInfo: String
    ): String = {
      val logoToUse = preprocessLogo(theLogo)
      raw"""$logoToUse
           |${projectName.blue} ${projectVersion.green}
           |${s"Scala $scalaVersion".yellow}
           |-----------------------------------------------------
           |$logoAdditionalInfo""".stripMargin
    }

    val sbtWelcomeAliasFormatter: String => String =
      _ + s"${scala.io.AnsiColor.RESET}: "

  }

  import autoImport.*
  import sbtwelcome.*

  override def buildSettings: Seq[Def.Setting[?]] = Seq(
    devOopsLogLevel := DevOopsLogLevel.info.render,
    starterWriteDefaultScalafmtConf := {
      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      val dialectVersion = s"scala${scalaBinaryVersion.value.replace(".", "")}"
      val baseDirFile    = (ThisBuild / baseDirectory).value
      val outFile        = baseDirFile / ".scalafmt.conf"

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
          case Left(err) => messageOnlyException(err.render)
          case Right(_) => log.info(s"The default ${outFile.getName.blue} file has been written.")
        }
      }

    },
    starterWriteDefaultScalafixConf := {
      implicit val devOopsLogLevelValue: DevOopsLogLevel = DevOopsLogLevel.fromStringUnsafe(devOopsLogLevel.value)

      val crossScalaVers = crossScalaVersions.value
      val scalaVer       = scalaVersion.value
      val baseDirFile    = (ThisBuild / baseDirectory).value

      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      import cats.effect.unsafe.implicits.global

      if (crossScalaVers.exists(_.startsWith("2.")) && crossScalaVers.exists(_.startsWith("3."))) {
        val (source1, out1) = ("default-scalafix-scala2.conf.template", ".scalafix-scala2.conf")
        val (source2, out2) = ("default-scalafix-scala3.conf.template", ".scalafix-scala3.conf")

        val outFile1 = baseDirFile / out1
        val outFile2 = baseDirFile / out2

        if (!outFile1.exists() && !outFile2.exists()) {
          (
            writeDefaultScalafixConf(source1, outFile1),
            writeDefaultScalafixConf(source2, outFile2)
          )
            .parMapN { (r1, r2) =>
              (r1.toEitherNec, r2.toEitherNec).parMapN((_, _))
            }
            .unsafeRunSync() match {
            case Left(errs) => messageOnlyException(errs.map(_.render).toList.mkString("[", ",", "]"))
            case Right((outFile1, outFile2)) =>
              log.info(s"The default ${outFile1.getName.blue} and ${outFile2.getName.blue} files have been written.")
          }
        } else {
          log.info(
            raw"""${outFile1.getName.blue} or ${outFile2.getName.blue} or both already exist so it will not overwrite.
                 |If you want to regenerate them, please remove the existing ones.
                 |""".stripMargin
          )
        }
      } else {
        val (source, out) =
          if (scalaVer.startsWith("2."))
            ("default-scalafix-scala2.conf.template", ".scalafix.conf")
          else
            ("default-scalafix-scala3.conf.template", ".scalafix.conf")
        val outFile       = baseDirFile / out

        if (outFile.exists()) {
          log.info(
            raw"""${outFile.getName.blue} already exists so it will not rewrite ${outFile.getName.blue}.
                 |If you want to regenerate it, please remove the existing one.
                 |""".stripMargin
          )
        } else {
          writeDefaultScalafixConf(source, outFile)
            .unsafeRunSync() match {
            case Left(err) => messageOnlyException(err.render)
            case Right(outFile) => log.info(s"The default ${outFile.getName.blue} file has been written.")
          }
        }
      }
    },
  )

  def writeDefaultScalafmtConf[F[?]: Fx: LogF: Async: Network: Files](dialectVersion: String, outFile: File)(
    implicit LV: DevOopsLogLevel
  ): F[Either[StarterError, Unit]] =
    EmberClientBuilder
      .default[F]
      .build
      .use { client =>
        (for {
          gitHubApi <- GitHubApi[F](HttpClient[F](client)).rightTF
          tags      <- gitHubApi
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
                                  ).catchNonFatal {
                                    case err =>
                                      StarterError.resourceReadWrite(
                                        s"reading the default .scalafmt.conf template file at resources/scalafmt/default-scalafmt.conf.template",
                                        err.toString
                                      )

                                  }.t
          scalafmtConf         <- pureOrError[F](
                                    scalafmtConfTemplate
                                      .replace("%SCALAFMT_VERSION%", theLatestScalafmtVersion)
                                      .replace("%DIALECT_VERSION%", dialectVersion)
                                  ).rightT[StarterError]
          _                    <- effectOf[F](
                                    SbtIO.write(outFile, scalafmtConf, StandardCharsets.UTF_8)
                                  ).catchNonFatal {
                                    case err =>
                                      StarterError.resourceReadWrite(
                                        s"writing the default .scalafmt.conf to ${outFile.toString}",
                                        err.toString
                                      )
                                  }.t
        } yield ()).value
      }

  def writeDefaultScalafixConf[F[?]: Fx: LogF: Async](
    templateFilename: String,
    outFile: File
  ): F[Either[StarterError, File]] =
    (for {
      scalafixConfTemplate <- effectOf[F](
                                SbtIO.readStream(
                                  this.getClass.getResourceAsStream(s"/scalafix/$templateFilename"),
                                  StandardCharsets.UTF_8
                                )
                              ).catchNonFatal {
                                case err =>
                                  StarterError.resourceReadWrite(
                                    s"reading the $templateFilename file at resources/scalafix/$templateFilename",
                                    err.toString
                                  )

                              }.t
      _                    <- effectOf[F](
                                SbtIO.write(outFile, scalafixConfTemplate, StandardCharsets.UTF_8)
                              ).catchNonFatal {
                                case err =>
                                  StarterError.resourceReadWrite(
                                    s"writing the default scalafix config file to ${outFile.toString}",
                                    err.toString
                                  )
                              }.t
    } yield outFile).value

  override def projectSettings: Seq[Setting[?]] = Seq(
    theLogo := DefaultLogo,
    logoAdditionalInfo := "",
    logo := genLogo(
      name.value,
      version.value,
      scalaVersion.value,
      theLogo.value,
      logoAdditionalInfo.value
    ),
    usefulTasks := Seq(
      UsefulTask("reload", "Run reload").alias("r"),
      UsefulTask("clean", "Run clean").alias("cln"),
      UsefulTask("compile", "Run compile").alias("c"),
      UsefulTask("+compile", "Run cross-scalaVersion compile").alias("cc"),
      UsefulTask("Test/compile", "Run Test/compile").alias("tc"),
      UsefulTask("+Test/compile", "Run cross-scalaVersion Test/compile").alias("ctc"),
      UsefulTask("test", "Run test").alias("t"),
      UsefulTask("+test", "Run cross-scalaVersion test").alias("ct"),
      UsefulTask("scalafmtCheckAll", "Run scalafmtCheckAll").alias("fmtchk"),
      UsefulTask("scalafmtAll", "Run scalafmtAll").alias("fmt"),
      UsefulTask("+scalafmtCheckAll", "Run +scalafmtCheckAll").alias("cfmtchk"),
      UsefulTask("+scalafmtAll", "Run +scalafmtAll").alias("cfmt"),
      UsefulTask("scalafixAll --check", "Run scalafixAll --check").alias("fixchk"),
      UsefulTask("scalafixAll", "Run scalafixAll").alias("fix"),
      UsefulTask("+scalafixAll --check", "Run +scalafixAll --check").alias("cfixchk"),
      UsefulTask("+scalafixAll", "Run +scalafixAll").alias("cfix"),
      UsefulTask("fmtchk; fixchk", "Run scalafmtCheckAll; scalafixAll --check").alias("chk"),
      UsefulTask("cfmtchk; cfixchk", "Run +scalafmtCheckAll; +scalafixAll --check").alias("cchk"),
      UsefulTask("publishLocal", "Run publishLocal").alias("pl"),
    ).map(_.formatAlias(sbtWelcomeAliasFormatter)),
    logoColor := Color.magenta.toAnsi,
    aliasColor := Color.blue.toAnsi,
  )

}
