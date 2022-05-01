ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / crossScalaVersions := List(
  "2.13.8",
  "2.12.15",
  "2.11.12",
)

lazy val root = (project in file("."))
  .settings(
    name                            := "scala-2-test",
    libraryDependencies ++= Dependencies.hedgehog,
    testFrameworks += TestFramework("hedgehog.sbt.Framework"),
    fork := true,
  )