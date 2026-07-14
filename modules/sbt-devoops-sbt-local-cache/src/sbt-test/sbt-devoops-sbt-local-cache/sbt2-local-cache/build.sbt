ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

Global / devOopsUseLocalCache := true

lazy val checkCacheNonEmpty = taskKey[Unit]("Check the build-local cache is not empty.")
lazy val checkCacheEmpty    = taskKey[Unit]("Check the build-local cache has no cached content.")

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-local-cache",
    checkCacheNonEmpty := Def.uncached {
      val dir   = (Global / devOopsBuildLocalCacheDirectory).value
      val files = (dir.allPaths --- dir.allPaths.filter(_.isDirectory)).get()
      if (files.isEmpty)
        sys.error(s"The build-local cache is expected to be non-empty but it is empty. (${dir.getAbsolutePath})")
      else ()
    },
    checkCacheEmpty := Def.uncached {
      val dir   = (Global / devOopsBuildLocalCacheDirectory).value
      val files = (dir.allPaths --- dir.allPaths.filter(_.isDirectory)).get()
      if (files.nonEmpty)
        sys.error(
          s"The build-local cache is expected to be empty but it has ${files.length} file(s). (${dir.getAbsolutePath})"
        )
      else ()
    },
  )
