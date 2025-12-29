ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "write-default-scalafix-conf-scala2-test-existing",
    fork := true,
  )
