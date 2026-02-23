import BuildTools._
import ProjectInfo._

ThisBuild / scalaVersion := props.ProjectScalaVersion
ThisBuild / crossSbtVersions := props.CrossSbtVersions
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  ),
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}"),
    s"git@github.com:${props.GitHubUsername}/${props.ProjectName}.git",
  ).some
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := 2018.some
ThisBuild / testFrameworks ~=
  (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct)

Global / sbtVersion := props.GlobalSbtVersion

lazy val sbtDevOops = Project(props.ProjectName, file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    organization := props.Org,
    name := props.ProjectName,
    description := "DevOops - DevOps tool for GitHub",
    writeVersion := versionWriter(Def.spaceDelimited("filename").parsed)(version.value),
    writeCurrentVersion := {
      val latestVersion = {
        import sys.process.*
        "git fetch --tags".!
        val tag = "git rev-list --tags --max-count=1".!!.trim
        s"git describe --tags $tag".!!.trim.stripPrefix("v")
      }
      val websiteDir    = docusaurDir.value

      val latestVersionFile = websiteDir / "latestVersion.json"
      val latestVersionJson = s"""{"version":"$latestVersion"}"""
      IO.write(latestVersionFile, latestVersionJson)
    },
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    gitHubPagesOrgName := props.GitHubUsername,
    gitHubPagesRepoName := props.ProjectName,
    publishMavenStyle := true,
  )
  .dependsOn(
    sbtDevOopsCommon,
    sbtDevOopsScala,
    sbtDevOopsSbtExtra,
    sbtDevOopsGitHub,
  )
  .aggregate(
    sbtDevOopsCommon,
    sbtDevOopsScala,
    sbtDevOopsSbtExtra,
    sbtDevOopsHttpCore,
    sbtDevOopsGitHubCore,
    sbtDevOopsStarter,
    sbtDevOopsGitHub,
    sbtDevOopsReleaseVersionPolicy,
    sbtDevOopsJava,
  )

lazy val sbtDevOopsCommon = subProject(props.SubProjectNameCommon)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= List(
      libs.semVer,
      libs.commonsIo,
      libs.cats,
      libs.newtype % Test,
    ) ++ libs.hedgehogLibs,
  )

lazy val sbtDevOopsScala = subProject(props.SubProjectNameScala)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtTpolecat)
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsSbtExtra = subProject(props.SubProjectNameSbtExtra)
  .enablePlugins(SbtPlugin)

lazy val sbtDevOopsHttpCore = subProject(props.SubProjectNameHttpCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= List(
      libs.catsEffect,
      libs.newtype,
      libs.effectie,
      libs.justSysprocess,
      libs.extrasCats,
    ) ++ libs.loggerF ++ libs.circe ++ libs.refined ++ libs.http4sClient ++ libs.javaxActivation212,
  )
  .dependsOn(sbtDevOopsCommon % props.IncludeTest)

lazy val sbtDevOopsGitHubCore = subProject(props.SubProjectNameGitHubCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= libs.hedgehogLibs ++ List(libs.extrasHedgehogCatsEffect3),
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsHttpCore)

lazy val sbtDevOopsStarter = subProject(props.SubProjectNameStarter)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtScalafmt),
    addSbtPlugin(libs.sbtScalafix),
    addSbtPlugin(libs.sbtWelcome),
    libraryDependencies ++= List(libs.extrasScalaIo)
  )
  .dependsOn(sbtDevOopsScala, sbtDevOopsSbtExtra, sbtDevOopsHttpCore, sbtDevOopsGitHubCore)

lazy val sbtDevOopsGitHub = subProject(props.SubProjectNameGitHub)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= List(
      libs.extrasScalaIo,
    )
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsGitHubCore)

lazy val sbtDevOopsReleaseVersionPolicy = subProject(props.SubProjectNameReleaseVersionPolicy)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtRelease),
    addSbtPlugin(libs.sbtVersionPolicy),
    libraryDependencies ++= List(
      libs.extrasScalaIo,
    )
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsJava = subProject(props.SubProjectNameJava)
  .enablePlugins(SbtPlugin)

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def subProject(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      organization := props.Org,
      name := prefixedName,
      addCompilerPlugin("org.scalamacros" % "paradise"       % "2.1.1" cross CrossVersion.full),
      addCompilerPlugin("org.typelevel"   % "kind-projector" % "0.13.4" cross CrossVersion.full),
//      scalacOptions ++= List("-Xsource:3"),
      Compile / console / scalacOptions := scalacOptions.value diff List("-Ywarn-unused-import", "-Xfatal-warnings"),
      Compile / compile / wartremoverErrors ++= commonWarts,
      Test / compile / wartremoverErrors ++= commonWarts,
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
      publishMavenStyle := true,
      coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          false
        case _ =>
          true
      }),
      scriptedLaunchOpts := {
        scriptedLaunchOpts.value ++
          Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog := false,
    )
}

lazy val props =
  new {

    private val GitHubRepo = findRepoOrgAndName

    val Org            = "io.kevinlee"
    val GitHubUsername = GitHubRepo.fold("Kevin-Lee")(_.orgToString)
    val RepoName       = GitHubRepo.fold("sbt-devoops")(_.nameToString)

    val ProjectName = RepoName

    val SubProjectNameCommon               = "common"
    val SubProjectNameScala                = "scala"
    val SubProjectNameSbtExtra             = "sbt-extra"
    val SubProjectNameStarter              = "starter"
    val SubProjectNameHttpCore             = "http-core"
    val SubProjectNameGitHubCore           = "github-core"
    val SubProjectNameGitHub               = "github"
    val SubProjectNameReleaseVersionPolicy = "release-version-policy"
    val SubProjectNameJava                 = "java"

    val ProjectScalaVersion = "2.12.18"
    val CrossScalaVersions  = List(ProjectScalaVersion).distinct

    val GlobalSbtVersion = "1.3.4"

    val CrossSbtVersions = List(GlobalSbtVersion).distinct

    val hedgehogVersion = "0.13.0"

    val newtypeVersion = "0.4.4"

    val catsVersion       = "2.13.0"
    val catsEffectVersion = "3.6.3"

    val extrasVersion = "0.50.1"

    val effectieVersion = "2.3.0"
    val loggerFVersion  = "2.8.1"

    val refinedVersion = "0.11.3"

    val circeVersion        = "0.14.15"
    val circeRefinedVersion = "0.15.1"

    val http4sVersion = "0.23.33"

    val justSemVerVersion = "1.1.1"

    val justSysprocessVersion = "1.0.0"

    val commonsIoVersion = "2.21.0"

    val activationVersion    = "1.1.1"
    val activationApiVersion = "1.2.0"

    val SbtTpolecatVersion = "0.5.2"

    val SbtVersionPolicyVersion = "3.2.1"
    val SbtReleaseVersion       = "1.4.0"

    val SbtScalafmtVersion = "2.5.6"
    val SbtScalafixVersion = "0.14.5"

    val SbtWelcomeVersion = "0.5.0"

    val IncludeTest = "compile->compile;test->test"
  }

lazy val libs =
  new {

    lazy val hedgehogLibs = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val newtype = "io.estatico" %% "newtype" % props.newtypeVersion

    lazy val refined = Seq(
      "eu.timepit" %% "refined"      % props.refinedVersion,
      "eu.timepit" %% "refined-cats" % props.refinedVersion,
    )

    lazy val cats       = "org.typelevel" %% "cats-core"   % props.catsVersion
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % props.catsEffectVersion

    lazy val extrasCats                = "io.kevinlee" %% "extras-cats"     % props.extrasVersion
    lazy val extrasScalaIo             = "io.kevinlee" %% "extras-scala-io" % props.extrasVersion
    lazy val extrasHedgehogCatsEffect3 =
      "io.kevinlee" %% "extras-hedgehog-ce3" % props.extrasVersion % Test

    lazy val effectie = "io.kevinlee" %% "effectie-cats-effect3" % props.effectieVersion

    lazy val loggerF = List(
      "io.kevinlee" %% "logger-f-cats"        % props.loggerFVersion,
      "io.kevinlee" %% "logger-f-sbt-logging" % props.loggerFVersion,
    )

    lazy val http4sClient = List(
      "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
      "org.http4s" %% "http4s-ember-client" % props.http4sVersion,
      "org.http4s" %% "http4s-circe"        % props.http4sVersion,
    )

    lazy val circe = List(
      "io.circe" %% "circe-generic" % props.circeVersion,
      "io.circe" %% "circe-parser"  % props.circeVersion,
    ) ++ List(
      "io.circe" %% "circe-refined" % props.circeRefinedVersion
    )

    lazy val semVer = "io.kevinlee" %% "just-semver" % props.justSemVerVersion

    lazy val justSysprocess = "io.kevinlee" %% "just-sysprocess" % props.justSysprocessVersion

    lazy val commonsIo = "commons-io" % "commons-io" % props.commonsIoVersion

    lazy val javaxActivation212 = List(
      "javax.activation" % "activation" % props.activationVersion,
    )

    lazy val sbtTpolecat = "org.typelevel" % "sbt-tpolecat" % props.SbtTpolecatVersion

    lazy val sbtVersionPolicy = "ch.epfl.scala"  % "sbt-version-policy" % props.SbtVersionPolicyVersion
    lazy val sbtRelease       = "com.github.sbt" % "sbt-release"        % props.SbtReleaseVersion

    lazy val sbtScalafmt = "org.scalameta" % "sbt-scalafmt" % props.SbtScalafmtVersion
    lazy val sbtScalafix = "ch.epfl.scala" % "sbt-scalafix" % props.SbtScalafixVersion

    lazy val sbtWelcome = "com.github.reibitto" % "sbt-welcome" % props.SbtWelcomeVersion

    def all(scalaVersion: String) = crossVersionProps(
      List(
        commonsIo,
        semVer,
        newtype,
        cats,
        catsEffect,
        effectie,
        justSysprocess,
        extrasScalaIo,
        extrasHedgehogCatsEffect3,
      ) ++
        hedgehogLibs ++
        loggerF ++
        http4sClient ++
        circe,
      scalaVersion,
    ) {
      case Some((2, 12)) =>
        javaxActivation212
      case Some((2, 10)) =>
        Seq.empty
    }
  }

lazy val writeVersion        = inputKey[Unit]("Write Version in File'")
lazy val writeCurrentVersion = inputKey[Unit]("Write the current version at ${docusaurDir.value}/latestVersion.json")

import scala.{Console => sConsole}
logo :=
  raw"""
       |       __   __      ___           ____
       |  ___ / /  / /_____/ _ \___ _  __/ __ \___  ___  ___
       | (_-</ _ \/ __/___/ // / -_) |/ / /_/ / _ \/ _ \(_-<
       |/___/_.__/\__/   /____/\__/|___/\____/\___/ .__/___/
       |                                         /_/
       |
       |${sConsole.BLUE}${name.value}${sConsole.RESET} v${sConsole.BLUE}${version.value}${sConsole.RESET}
       |${sConsole.YELLOW}Scala ${scalaVersion.value}${sConsole.RESET}
       |-----------------------------------------------------
       |""".stripMargin

import sbtwelcome._

val aliasFormatter: String => String =
  _ + s"${scala.io.AnsiColor.RESET}: "

usefulTasks := Seq(
  UsefulTask("reload", "Run reload").alias("r"),
  UsefulTask("clean", "Run clean").alias("cln"),
  UsefulTask("compile", "Run compile").alias("c"),
  UsefulTask("Test/compile", "Run Test/compile").alias("tc"),
  UsefulTask("test", "Run test").alias("t"),
  UsefulTask("scripted", "Run scripted for sbt-test").alias("st"),
  UsefulTask("scalafmtCheckAll", "Run scalafmtCheckAll").alias("fmtchk"),
  UsefulTask("scalafmtAll", "Run scalafmtAll").alias("fmt"),
  UsefulTask("publishLocal", "Run publishLocal").alias("pl"),
  UsefulTask("dependencyUpdates", "Run dependencyUpdates").alias("du"),
  UsefulTask("unusedCompileDependencies", "Run unusedCompileDependencies").alias("uud"),
  UsefulTask("undeclaredCompileDependencies", "Run undeclaredCompileDependencies").alias("udd"),
).map(_.formatAlias(aliasFormatter))

logoColor := sConsole.MAGENTA
