ThisBuild / scalaVersion     := "3.0.0"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "write-default-scalafix-conf-scala3-test-new",
    fork := true,
  )
