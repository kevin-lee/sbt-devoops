ThisBuild / scalaVersion     := "2.13.9"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / crossScalaVersions := List(
  "2.13.9",
  "2.12.17",
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-2-test",
    libraryDependencies ++= Dependencies.hedgehog,
    testFrameworks += TestFramework("hedgehog.sbt.Framework"),
    fork := true,
  )
