import BuildTools._
import ProjectInfo._
import sbt.ScmInfo

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    organization := "io.kevinlee",
    name := props.ProjectName,
    scalaVersion := props.ProjectScalaVersion,
    description := "DevOops - DevOps tool for GitHub",
    developers := List(
      Developer(
        props.GitHubUsername,
        "Kevin Lee",
        "kevin.code@kevinlee.io",
        url(s"https://github.com/${props.GitHubUsername}"),
      )
    ),
    homepage := url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}").some,
    scmInfo :=
      ScmInfo(
        url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}"),
        s"git@github.com:${props.GitHubUsername}/${props.ProjectName}.git",
      ).some,
    startYear := 2018.some,
    Global / sbtVersion := props.GlobalSbtVersion,
    crossSbtVersions := props.CrossSbtVersions,
    scalacOptions ++= crossVersionProps(commonScalacOptions, scalaVersion.value) {
      case Some((2, 12)) =>
        Seq("-Ywarn-unused-import", "-Ywarn-numeric-widen", "-language:implicitConversions")
      case Some((2, 11)) =>
        Seq("-Ywarn-numeric-widen")
      case _             =>
        Nil
    },
    Compile / console / scalacOptions := scalacOptions.value diff List("-Ywarn-unused-import", "-Xfatal-warnings"),
    Compile / compile / wartremoverErrors ++= commonWarts,
    Test / compile / wartremoverErrors ++= commonWarts,
    libraryDependencies ++=
      crossVersionProps(
        List(
          libs.commonsIo,
          libs.semVer,
          libs.newtype,
          libs.cats,
          libs.catsEffect,
          libs.effectie,
        ) ++
          libs.hedgehogLibs ++
          libs.loggerF ++
          libs.http4sClient ++
          libs.circe,
        scalaVersion.value,
      ) {
        case Some((2, 12)) =>
          libs.javaxActivation212
        case Some((2, 10)) =>
          Seq.empty
      },
    testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework")),
    licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    publishMavenStyle := true,
    console / initialCommands := """import kevinlee.sbt._""",
    writeVersion := versionWriter(Def.spaceDelimited("filename").parsed)(version.value),
    coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        false
      case _             =>
        true
    }),
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
    gitHubPagesOrgName := props.GitHubUsername,
    gitHubPagesRepoName := props.ProjectName,
  )

lazy val props =
  new {

    final val GitHubUsername = "Kevin-Lee"
    final val ProjectName    = "sbt-devoops"

    final val ProjectScalaVersion = "2.12.12"
    final val CrossScalaVersions  = List(ProjectScalaVersion).distinct

    final val GlobalSbtVersion = "1.3.4"

    final val CrossSbtVersions = List(GlobalSbtVersion).distinct

    final val hedgehogVersion = "0.7.0"

    final val newtypeVersion = "0.4.4"

    final val catsVersion       = "2.6.1"
    final val catsEffectVersion = "2.5.1"

    final val effectieVersion = "1.11.0"
    final val loggerFVersion  = "1.11.0"

    final val refinedVersion = "0.9.25"

    final val circeVersion = "0.13.0"

    final val http4sVersion = "0.21.23"

    final val justSemVerVersion = "0.2.0"

    final val commonsIoVersion = "2.8.0"

    final val activationVersion    = "1.1.1"
    final val activationApiVersion = "1.2.0"

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

    lazy val effectie = "io.kevinlee" %% "effectie-cats-effect" % props.effectieVersion

    lazy val loggerF = List(
      "io.kevinlee" %% "logger-f-cats-effect" % props.loggerFVersion,
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
      "io.circe" %% "circe-literal" % props.circeVersion,
      "io.circe" %% "circe-refined" % props.circeVersion,
    )

    lazy val semVer = "io.kevinlee" %% "just-semver" % props.justSemVerVersion

    lazy val commonsIo = "commons-io" % "commons-io" % props.commonsIoVersion

    lazy val javaxActivation212 = List(
      "javax.activation" % "activation"           % props.activationVersion,
      "javax.activation" % "javax.activation-api" % props.activationApiVersion,
    )

  }

lazy val writeVersion = inputKey[Unit]("Write Version in File'")
