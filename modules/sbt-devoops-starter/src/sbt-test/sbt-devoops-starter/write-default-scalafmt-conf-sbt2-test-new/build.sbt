ThisBuild / scalaVersion     := "3.8.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "write-default-scalafmt-conf-sbt2-test-new",
    fork := true,
  )
