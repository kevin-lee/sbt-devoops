ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

Global / devOopsAlwaysShowOnLoadMessage := true

lazy val checkFlagIsOn = taskKey[Unit]("Check devOopsAlwaysShowOnLoadMessage is on.")

/* The banner itself is only emitted on an interactive prompt render, which scripted cannot drive,
 * so these tests cover the wiring: the flag applies, onLoadMessage is still readable, and the
 * prompt override does not break loading or running the build.
 */
lazy val checkOnLoadMessageIsReadable = taskKey[Unit]("Check onLoadMessage is non-empty and readable.")

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-always-show-enabled",
    checkFlagIsOn := Def.uncached {
      val flag = (Global / devOopsAlwaysShowOnLoadMessage).value
      if (!flag)
        sys.error("devOopsAlwaysShowOnLoadMessage is expected to be true but it is false.")
      else ()
    },
    checkOnLoadMessageIsReadable := Def.uncached {
      val message = onLoadMessage.value
      if (message.isEmpty)
        sys.error("onLoadMessage is expected to be non-empty.")
      else ()
    },
  )
