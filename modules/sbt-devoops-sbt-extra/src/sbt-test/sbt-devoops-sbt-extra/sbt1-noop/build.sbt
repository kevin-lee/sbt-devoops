ThisBuild / scalaVersion     := "2.12.18"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

/* The key exists on both axes so that a shared build definition stays source compatible, but on
 * sbt 1 it does nothing: sbt 1 has no thin client, so it always loads the project and always
 * prints onLoadMessage.
 */
Global / devOopsAlwaysShowOnLoadMessage := true

lazy val root = (project in file("."))
  .settings(
    name := "sbt1-noop"
  )
