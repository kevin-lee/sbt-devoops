ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val checkCacheNotBuildLocal = taskKey[Unit]("Check the sbt local cache is not build-local.")

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-default-off",
    checkCacheNotBuildLocal := Def.uncached {
      val cacheDir     = (Global / localCacheDirectory).value
      val baseDir      = (ThisBuild / baseDirectory).value
      val buildLocalCache = baseDir / ".sbt-local-cache"
      /* scripted redirects the sbt global base into the test's temp directory,
       * so the default (machine-wide) cache path can legitimately live under baseDir.
       * Only the plugin's own build-local path counts as build-local here.
       */
      if (cacheDir.getAbsoluteFile.toPath.normalize == buildLocalCache.getAbsoluteFile.toPath.normalize)
        sys.error(
          s"The sbt local cache is expected to be machine-wide but it is build-local. (${cacheDir.getAbsolutePath})"
        )
      else if (buildLocalCache.exists)
        sys.error(s"The build-local cache directory is expected not to exist. (${buildLocalCache.getAbsolutePath})")
      else ()
    },
  )
