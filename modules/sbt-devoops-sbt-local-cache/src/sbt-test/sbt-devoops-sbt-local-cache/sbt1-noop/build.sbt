ThisBuild / scalaVersion     := "2.12.18"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

Global / devOopsUseLocalCache := true

lazy val root = (project in file("."))
  .settings(
    name := "sbt1-noop"
  )
