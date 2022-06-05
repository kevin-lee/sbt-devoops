import BuildTools._
import ProjectInfo._

ThisBuild / scalaVersion     := props.ProjectScalaVersion
ThisBuild / crossSbtVersions := props.CrossSbtVersions
ThisBuild / developers       := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  ),
)
ThisBuild / homepage         := url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}").some
ThisBuild / scmInfo          :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}"),
    s"git@github.com:${props.GitHubUsername}/${props.ProjectName}.git",
  ).some
ThisBuild / licenses         := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear        := 2018.some
ThisBuild / testFrameworks ~=
  (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct)
ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

Global / sbtVersion          := props.GlobalSbtVersion

lazy val sbtDevOops = Project(props.ProjectName, file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    organization        := props.Org,
    name                := props.ProjectName,
    description         := "DevOops - DevOps tool for GitHub",
    writeVersion        := versionWriter(Def.spaceDelimited("filename").parsed)(version.value),
    docusaurDir         := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir    := docusaurDir.value / "build",
    gitHubPagesOrgName  := props.GitHubUsername,
    gitHubPagesRepoName := props.ProjectName,
    publishMavenStyle   := true,
  )
  .settings(mavenCentralPublishSettings)
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
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsSbtExtra = subProject(props.SubProjectNameSbtExtra)
  .enablePlugins(SbtPlugin)

lazy val sbtDevOopsHttpCore = subProject(props.SubProjectNameHttpCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= libs.all(scalaVersion.value),
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsGitHubCore = subProject(props.SubProjectNameGitHubCore)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= libs.all(scalaVersion.value),
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsHttpCore)

lazy val sbtDevOopsStarter = subProject(props.SubProjectNameStarter)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtScalafmt),
    addSbtPlugin(libs.sbtScalafix),
    addSbtPlugin(libs.sbtWelcome),
  )
  .dependsOn(sbtDevOopsScala, sbtDevOopsSbtExtra, sbtDevOopsHttpCore, sbtDevOopsGitHubCore)

lazy val sbtDevOopsGitHub = subProject(props.SubProjectNameGitHub)
  .enablePlugins(SbtPlugin)
  .settings(
    libraryDependencies ++= libs.all(scalaVersion.value),
  )
  .dependsOn(sbtDevOopsCommon, sbtDevOopsGitHubCore)

lazy val sbtDevOopsReleaseVersionPolicy = subProject(props.SubProjectNameReleaseVersionPolicy)
  .enablePlugins(SbtPlugin)
  .settings(
    addSbtPlugin(libs.sbtRelease),
    addSbtPlugin(libs.sbtVersionPolicy),
    libraryDependencies ++= List(
      libs.extrasCats,
      libs.extrasScalaIo,
    )
  )
  .dependsOn(sbtDevOopsCommon)

lazy val sbtDevOopsJava = subProject(props.SubProjectNameJava)
  .enablePlugins(SbtPlugin)

lazy val mavenCentralPublishSettings: SettingsDefinition = List(
  /* Publish to Maven Central { */
  sonatypeCredentialHost := props.SonatypeCredentialHost,
  sonatypeRepository     := props.SonatypeRepository,
  /* } Publish to Maven Central */
)

// scalafmt: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// scalafmt: on

def subProject(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      organization                      := props.Org,
      name                              := prefixedName,
      Compile / console / scalacOptions := scalacOptions.value diff List("-Ywarn-unused-import", "-Xfatal-warnings"),
      Compile / compile / wartremoverErrors ++= commonWarts,
      Test / compile / wartremoverErrors ++= commonWarts,
      testFrameworks ~=
        (frameworks => (TestFramework("hedgehog.sbt.Framework") +: frameworks).distinct),
      licenses                          := List("MIT" -> url("http://opensource.org/licenses/MIT")),
      publishMavenStyle                 := true,
      coverageHighlighting              := (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 10)) =>
          false
        case _ =>
          true
      }),
      scriptedLaunchOpts                := {
        scriptedLaunchOpts.value ++
          Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
      },
      scriptedBufferLog                 := false,
    )
    .settings(mavenCentralPublishSettings)
}

lazy val props =
  new {

    private val GitHubRepo = findRepoOrgAndName

    final val Org      = "io.kevinlee"
    val GitHubUsername = GitHubRepo.fold("Kevin-Lee")(_.orgToString)
    val RepoName       = GitHubRepo.fold("sbt-devoops")(_.nameToString)

    final val ProjectName = RepoName

    val SonatypeCredentialHost = "s01.oss.sonatype.org"
    val SonatypeRepository     = s"https://$SonatypeCredentialHost/service/local"

    final val SubProjectNameCommon               = "common"
    final val SubProjectNameScala                = "scala"
    final val SubProjectNameSbtExtra             = "sbt-extra"
    final val SubProjectNameStarter              = "starter"
    final val SubProjectNameHttpCore             = "http-core"
    final val SubProjectNameGitHubCore           = "github-core"
    final val SubProjectNameGitHub               = "github"
    final val SubProjectNameReleaseVersionPolicy = "release-version-policy"
    final val SubProjectNameJava                 = "java"

    final val ProjectScalaVersion = "2.12.12"
    final val CrossScalaVersions  = List(ProjectScalaVersion).distinct

    final val GlobalSbtVersion = "1.3.4"

    final val CrossSbtVersions = List(GlobalSbtVersion).distinct

    final val hedgehogVersion = "0.8.0"

    final val newtypeVersion = "0.4.4"

    final val catsVersion       = "2.7.0"
    final val catsEffectVersion = "3.3.5"

    final val extrasVersion = "0.13.0"

    final val effectieVersion = "2.0.0-beta1"
    final val loggerFVersion  = "2.0.0-beta1"

    final val refinedVersion = "0.9.28"

    final val circeVersion = "0.14.1"

    final val http4sVersion = "0.23.11"

    final val justSemVerVersion = "0.4.0"

    final val justSysprocessVersion = "1.0.0"

    final val commonsIoVersion = "2.11.0"

    final val activationVersion    = "1.1.1"
    final val activationApiVersion = "1.2.0"

    val SbtVersionPolicyVersion = "2.0.1"
    val SbtReleaseVersion       = "1.1.0"

    val SbtScalafmtVersion = "2.4.6"
    val SbtScalafixVersion = "0.10.0"

    val SbtWelcomeVersion = "0.2.2"

    final val IncludeTest = "compile->compile;test->test"
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
      "io.kevinlee" %% "extras-hedgehog-cats-effect3" % props.extrasVersion % Test

    lazy val effectie = "io.kevinlee" %% "effectie-cats-effect3" % props.effectieVersion

    lazy val loggerF = List(
      "io.kevinlee" %% "logger-f-cats"        % props.loggerFVersion,
      "io.kevinlee" %% "logger-f-sbt-logging" % props.loggerFVersion,
    )

    lazy val http4sClient = List(
      "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % props.http4sVersion,
      "org.http4s" %% "http4s-circe"        % props.http4sVersion,
    )

    lazy val circe = List(
      "io.circe" %% "circe-generic" % props.circeVersion,
      "io.circe" %% "circe-parser"  % props.circeVersion,
      "io.circe" %% "circe-refined" % props.circeVersion,
    )

    lazy val semVer = "io.kevinlee" %% "just-semver" % props.justSemVerVersion

    lazy val justSysprocess = "io.kevinlee" %% "just-sysprocess" % props.justSysprocessVersion

    lazy val commonsIo = "commons-io" % "commons-io" % props.commonsIoVersion

    lazy val javaxActivation212 = List(
      "javax.activation" % "activation" % props.activationVersion,
    )

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

lazy val writeVersion = inputKey[Unit]("Write Version in File'")

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

usefulTasks := Seq(
  UsefulTask("r", "reload", "Run reload"),
  UsefulTask("cln", "clean", "Run clean"),
  UsefulTask("c", "compile", "Run compile"),
  UsefulTask("tc", "Test/compile", "Run Test/compile"),
  UsefulTask("t", "test", "Run test"),
  UsefulTask("st", "scripted", "Run scripted for sbt-test"),
  UsefulTask("fmtchk", "scalafmtCheckAll", "Run scalafmtCheckAll"),
  UsefulTask("fmt", "scalafmtAll", "Run scalafmtAll"),
  UsefulTask("pl", "publishLocal", "Run publishLocal"),
)

logoColor := sConsole.MAGENTA
