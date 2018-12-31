import BuildTools._
import ProjectInfo._
import sbt.Path

import org.scoverage.coveralls.Imports.CoverallsKeys._

lazy val writeVersion = inputKey[Unit]("Write Version in File'")

lazy val root = (project in file("."))
  .settings(
    organization := "io.kevinlee"
  , name         := "sbt-devoops"
  , scalaVersion := ProjectScalaVersion
  , version      := ProjectVersion
  , description  := "DevOops - DevOps tool for GitHub"
  , developers   := List(
      Developer("Kevin-Lee", "Kevin Lee", "kevin.code@kevinlee.io", url("https://github.com/Kevin-Lee"))
    )
  , sbtPlugin := true
  , sbtVersion in Global := "1.2.6"
  , scalaCompilerBridgeSource := {
      val sv = appConfiguration.value.provider.id.version
      ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
    }
  , crossSbtVersions := CrossSbtVersions
  , scalacOptions ++=
      crossVersionProps(commonScalacOptions, scalaVersion.value) {
        case Some((2, 12)) =>
          Seq("-Ywarn-unused-import", "-Ywarn-numeric-widen")
        case Some((2, 11)) =>
          Seq("-Ywarn-numeric-widen")
        case _ =>
          Nil
      }
  , wartremoverErrors in (Compile, compile) ++= commonWarts
  , wartremoverErrors in (Test, compile) ++= commonWarts
  , resolvers += Deps.hedgehogRepo
  , libraryDependencies ++= Seq(Deps.commonsIo) ++ Deps.hedgehogLibs
  , testFrameworks ++= Seq(TestFramework("hedgehog.sbt.Framework"))

//  , addSbtPlugin(Deps.wartRemover)
//  , addSbtPlugin(Deps.scoverage)
//  , addSbtPlugin(Deps.bintray)

  , bintrayPackageLabels := Seq("sbt", "plugin")
  , bintrayVcsUrl := Some("""git@github.com:Kevin-Lee/sbt-devoops.git""")

  , initialCommands in console := """import io.kevinlee.sbt._"""

  , writeVersion := versionWriter(() => Def.spaceDelimited("filename").parsed)(ProjectVersion)

  , coverageHighlighting := (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        false
      case _ =>
        true
    })
  , coverallsTokenFile := Option(s"""${Path.userHome.absolutePath}/.coveralls-credentials""")

// set up 'scripted; sbt plugin for testing sbt plugins
//  , scriptedLaunchOpts ++=
//      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)

)
