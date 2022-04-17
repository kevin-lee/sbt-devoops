ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

ThisBuild / crossScalaVersions := List(
  "3.1.2",
  "3.0.2",
)

lazy val root = (project in file("."))
  .settings(
    name                            := "scala-2-test",
    libraryDependencies ++= Dependencies.hedgehog,
    testFrameworks += TestFramework("hedgehog.sbt.Framework"),
    fork := true,
  )
