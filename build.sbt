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
    addCompilerPlugin("org.scalamacros" % "paradise"           % "2.1.1" cross CrossVersion.full),
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
    addCompilerPlugin("org.typelevel"   % "kind-projector"     % "0.11.3" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1"),
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

    val GitHubUsername: String = "Kevin-Lee"
    val ProjectName: String    = "sbt-devoops"

    val ProjectScalaVersion: String     = "2.12.12"
    val CrossScalaVersions: Seq[String] = List(ProjectScalaVersion).distinct

    val GlobalSbtVersion: String = "1.3.4"

    val CrossSbtVersions: Seq[String] = List(GlobalSbtVersion).distinct

    val hedgehogVersion: String = "0.6.7"

    val catsVersion       = "2.6.0"
    val catsEffectVersion = "2.5.0"

    val effectieVersion = "1.10.0"
    val loggerFVersion  = "1.10.0"

    val refinedVersion = "0.9.24"

    val circeVersion = "0.13.0"

    val http4sVersion = "0.21.22"

    val IncludeTest: String = "compile->compile;test->test"
  }

lazy val libs =
  new {

    lazy val hedgehogLibs = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val newtype = "io.estatico" %% "newtype" % "0.4.4"

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

    lazy val semVer = "io.kevinlee" %% "just-semver" % "0.1.2"

    lazy val commonsIo = "commons-io" % "commons-io" % "2.1"

    lazy val javaxActivation212 = List(
      "javax.activation" % "activation"           % "1.1.1",
      "javax.activation" % "javax.activation-api" % "1.2.0",
    )

  }

lazy val writeVersion = inputKey[Unit]("Write Version in File'")
