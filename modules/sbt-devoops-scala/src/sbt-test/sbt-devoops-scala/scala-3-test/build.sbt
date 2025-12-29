ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / crossScalaVersions := List(
  "3.3.5",
  "3.7.3",
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-3-test",
    libraryDependencies ++= Dependencies.hedgehog,
    testFrameworks += TestFramework("hedgehog.sbt.Framework"),
    fork := true,
  )
