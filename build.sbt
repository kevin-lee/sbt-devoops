import BuildTools._
import ProjectInfo._
import sbt.ScmInfo

lazy val root = (project in file("."))
  .enablePlugins(DocusaurPlugin)
  .settings(
    organization := "io.kevinlee"
  , name         := props.ProjectName
  , scalaVersion := props.ProjectScalaVersion
  , version      := ProjectVersion
  , description  := "DevOops - DevOps tool for GitHub"
  , developers   := List(
      Developer(props.GitHubUsername, "Kevin Lee", "kevin.code@kevinlee.io", url(s"https://github.com/${props.GitHubUsername}"))
    )
  , homepage := Some(url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}"))
  , scmInfo :=
      Some(ScmInfo(
        url(s"https://github.com/${props.GitHubUsername}/${props.ProjectName}")
      , s"git@github.com:${props.GitHubUsername}/${props.ProjectName}.git"
    ))

  , startYear := Some(2018)
  , sbtPlugin := true
  , sbtVersion in Global := props.GlobalSbtVersion
  , crossSbtVersions := props.CrossSbtVersions
  , addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  , scalacOptions ++= crossVersionProps(commonScalacOptions, scalaVersion.value) {
        case Some((2, 12)) =>
          Seq("-Ywarn-unused-import", "-Ywarn-numeric-widen", "-language:implicitConversions")
        case Some((2, 11)) =>
          Seq("-Ywarn-numeric-widen")
        case _ =>
          Nil
      }
  , scalacOptions in (Compile, console) := scalacOptions.value diff List("-Ywarn-unused-import", "-Xfatal-warnings")
  , wartremoverErrors in (Compile, compile) ++= commonWarts
  , wartremoverErrors in (Test, compile) ++= commonWarts
  , resolvers += props.hedgehogRepo
  , addCompilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary)
  , addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1")
  , libraryDependencies ++=
      crossVersionProps(List(
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
            libs.circe
        , scalaVersion.value
      ) {
        case Some((2, 12)) =>
          libs.javaxActivation212
        case Some((2, 10)) =>
          Seq.empty
      }
  , testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework"))

  , licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
  , publishMavenStyle := false

  , bintrayPackageLabels := Seq("sbt", "plugin")
  , bintrayVcsUrl := Some(s"""git@github.com:${props.GitHubUsername}/${props.ProjectName}.git""")
  , bintrayRepository := "sbt-plugins"

  , initialCommands in console := """import kevinlee.sbt._"""

  , writeVersion := versionWriter(Def.spaceDelimited("filename").parsed)(ProjectVersion)

  , coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        false
      case _ =>
        true
    })
  , docusaurDir := (ThisBuild / baseDirectory).value / "website"
  , docusaurBuildDir := docusaurDir.value / "build"

  , gitHubPagesOrgName := props.GitHubUsername
  , gitHubPagesRepoName := props.ProjectName

  )

lazy val props = new {

    val GitHubUsername: String = "Kevin-Lee"
    val ProjectName: String = "sbt-devoops"

    val ProjectScalaVersion: String = "2.12.12"
    val CrossScalaVersions: Seq[String] = Seq(ProjectScalaVersion).distinct

    val GlobalSbtVersion: String = "1.3.4"

    val CrossSbtVersions: Seq[String] = Seq(GlobalSbtVersion).distinct

    val hedgehogVersion: String = "0.6.1"

    val hedgehogRepo: Resolver =
      "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

    val catsVersion = "2.3.1"

    val catsEffectVersion = "2.3.1"

    val effectieVersion = "1.8.0"

    val loggerFVersion = "1.7.0"

    val refinedVersion = "0.9.19"

    val circeVersion = "0.13.0"

    val http4sVersion   = "0.21.16"

    val IncludeTest: String = "compile->compile;test->test"
  }

lazy val libs = new {

  val hedgehogLibs: Seq[ModuleID] = Seq(
    "qa.hedgehog" %% "hedgehog-core" % props.hedgehogVersion % Test
    , "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test
    , "qa.hedgehog" %% "hedgehog-sbt" % props.hedgehogVersion % Test
  )

  val newtype: ModuleID = "io.estatico" %% "newtype" % "0.4.4"

  lazy val refined  = Seq(
    "eu.timepit" %% "refined" % props.refinedVersion,
    "eu.timepit" %% "refined-cats" % props.refinedVersion,
  )

  val cats: ModuleID = "org.typelevel" %% "cats-core" % props.catsVersion

  val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % props.catsEffectVersion

  val effectie: ModuleID = "io.kevinlee" %% "effectie-cats-effect" % props.effectieVersion

  val loggerF: List[ModuleID] = List(
      "io.kevinlee" %% "logger-f-cats-effect" % props.loggerFVersion,
      "io.kevinlee" %% "logger-f-sbt-logging" % props.loggerFVersion,
    )

  lazy val http4sClient: List[ModuleID] = List(
    "org.http4s" %% "http4s-dsl"          % props.http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % props.http4sVersion,
    "org.http4s" %% "http4s-circe"        % props.http4sVersion,
  )

  lazy val circe: List[ModuleID] = List(
    "io.circe" %% "circe-generic" % props.circeVersion,
    "io.circe" %% "circe-parser" % props.circeVersion,
    "io.circe" %% "circe-literal" % props.circeVersion,
    "io.circe" %% "circe-refined" % props.circeVersion
  )

  val semVer: ModuleID = "io.kevinlee" %% "just-semver" % "0.1.0"

  val commonsIo: ModuleID = "commons-io" % "commons-io" % "2.1"

  val javaxActivation212: List[ModuleID] = List(
    "javax.activation" % "activation" % "1.1.1",
    "javax.activation" % "javax.activation-api" % "1.2.0"
  )

}

lazy val writeVersion = inputKey[Unit]("Write Version in File'")
