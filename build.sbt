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
  , scalacOptions ++= crossVersionProps(commonScalacOptions, scalaVersion.value) {
        case Some((2, 12)) =>
          Seq("-Ywarn-unused-import", "-Ywarn-numeric-widen")
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
  , libraryDependencies ++=
      crossVersionProps(
          Seq(
            libs.commonsIo, libs.githubApi, libs.semVer
          ) ++ libs.hedgehogLibs
        , scalaVersion.value
      ) {
        case Some((2, 12)) =>
          libs.javaxActivation212 ++ List(libs.cats)
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

  val ProjectScalaVersion: String = "2.12.10"
  val CrossScalaVersions: Seq[String] = Seq(ProjectScalaVersion).distinct

  val GlobalSbtVersion: String = "1.3.4"

  val CrossSbtVersions: Seq[String] = Seq(GlobalSbtVersion).distinct

  val hedgehogVersion: String = "64eccc9ca7dbe7a369208a14a97a25d7ccbbda67"

  val hedgehogRepo: Resolver =
    "bintray-scala-hedgehog" at "https://dl.bintray.com/hedgehogqa/scala-hedgehog"

}

lazy val libs = new {

  val hedgehogLibs: Seq[ModuleID] = Seq(
    "qa.hedgehog" %% "hedgehog-core" % props.hedgehogVersion % Test
    , "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test
    , "qa.hedgehog" %% "hedgehog-sbt" % props.hedgehogVersion % Test
  )

  val cats: ModuleID = "org.typelevel" %% "cats-core" % "2.3.1"

  val semVer: ModuleID = "io.kevinlee" %% "just-semver" % "0.1.0"

  val commonsIo: ModuleID = "commons-io" % "commons-io" % "2.1"

  val githubApi: ModuleID = "org.kohsuke" % "github-api" % "1.95"

  val javaxActivation212: List[ModuleID] = List(
    "javax.activation" % "activation" % "1.1.1"
    , "javax.activation" % "javax.activation-api" % "1.2.0"
  )

}

lazy val writeVersion = inputKey[Unit]("Write Version in File'")
