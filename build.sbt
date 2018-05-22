import BuildTools._
import Dependencies._
import ProjectInfo._

lazy val root = (project in file(".")).
  settings(
    organization := "io.kevinlee",
    name         := "sbt-devoops",
    scalaVersion := ProjectScalaVersion,
    version      := ProjectVersion,
    description  := "DevOops - DevOps tool for GitHub",
    developers   := List(
      Developer("Kevin-Lee", "Kevin Lee", "kevin.code@kevinlee.io", url("https://github.com/Kevin-Lee"))
    ),

    sbtPlugin := true,

    sbtVersion in Global := "1.1.5",
    scalaCompilerBridgeSource := {
      val sv = appConfiguration.value.provider.id.version
      ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
    },

    crossSbtVersions := CrossSbtVersions,

    scalacOptions ++=
      crossVersionProps(commonScalacOptions, scalaVersion.value) {
        case Some((2, 12)) =>
          Seq("-Ywarn-unused-import")
        case _ =>
          Nil
      },

    wartremoverErrors in (Compile, compile) ++= commonWarts,
    wartremoverErrors in (Test, compile) ++= commonWarts,

    libraryDependencies ++= crossVersionProps(
      Seq(
        "commons-io" % "commons-io" % "2.1"
      ),
      scalaVersion.value
    ) {
      case Some((2, 12)) => dependencyLibs2_12
      case _ => dependencyLibs2_10
    },

    addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.1"),

    addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1"),

//    crossVersionSbtPlugin("com.sksamuel.scapegoat", "sbt-scapegoat") {
//      case Some((2, 12)) => "1.0.9"
//      case _ => "1.0.4"
//    },

    bintrayPackageLabels := Seq("sbt", "plugin"),
    bintrayVcsUrl := Some("""git@github.com:Kevin-Lee/sbt-devoops.git"""),

    initialCommands in console := """import io.kevinlee.sbt._"""//,

    // set up 'scripted; sbt plugin for testing sbt plugins
//    scriptedLaunchOpts ++=
//      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

)


