ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val checkFlagIsOffByDefault = taskKey[Unit]("Check devOopsAlwaysShowOnLoadMessage defaults to false.")

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-always-show-default-off",
    checkFlagIsOffByDefault := Def.uncached {
      val flag = (Global / devOopsAlwaysShowOnLoadMessage).value
      if (flag)
        sys.error("devOopsAlwaysShowOnLoadMessage is expected to default to false but it is true.")
      else ()
    },
  )
