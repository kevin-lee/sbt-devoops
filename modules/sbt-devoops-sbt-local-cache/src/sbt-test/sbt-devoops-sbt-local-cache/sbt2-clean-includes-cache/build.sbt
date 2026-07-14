ThisBuild / scalaVersion     := "3.3.5"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

Global / devOopsUseLocalCache := true

Global / devOopsCleanIncludesLocalCache := true

lazy val checkCacheNonEmpty = taskKey[Unit]("Check the build-local cache has the cached content.")
lazy val checkCachePurged   = taskKey[Unit]("Check the build-local cache has no cached content.")
lazy val writeHistory       = taskKey[Unit]("Write a sentinel shell history file into the output directory.")
lazy val checkHistoryKept   = taskKey[Unit]("Check the sentinel shell history file survived the cache purge.")

// The sbt 2 shell command history lives at `<rootOutputDirectory>/.history`, inside the output
// directory that a cache purge deletes. Purging the cache must not wipe it.
lazy val HistoryContent = "1700000000000:compile\n1700000000001:test\n"

// `clean` re-caches its own result after removing the cache, so a few entries for `clean` itself
// may remain. It is only the cached content of the actual work (e.g. compile) which must be gone.
lazy val MaxFilesWhenPurged = 3

lazy val root = (project in file("."))
  .settings(
    name := "sbt2-clean-includes-cache",
    checkCacheNonEmpty := Def.uncached {
      val dir   = (Global / devOopsBuildLocalCacheDirectory).value
      val files = (dir.allPaths --- dir.allPaths.filter(_.isDirectory)).get()
      if (files.length <= MaxFilesWhenPurged)
        sys.error(
          s"The build-local cache is expected to have the cached content but it has only ${files.length} file(s). (${dir.getAbsolutePath})"
        )
      else ()
    },
    checkCachePurged := Def.uncached {
      val dir   = (Global / devOopsBuildLocalCacheDirectory).value
      val files = (dir.allPaths --- dir.allPaths.filter(_.isDirectory)).get()
      if (files.length > MaxFilesWhenPurged)
        sys.error(
          s"The build-local cache is expected to be purged but it still has ${files.length} file(s). (${dir.getAbsolutePath})"
        )
      else ()
    },
    writeHistory := Def.uncached {
      val history = (Global / rootOutputDirectory).value.toFile / ".history"
      IO.write(history, HistoryContent)
    },
    checkHistoryKept := Def.uncached {
      val history = (Global / rootOutputDirectory).value.toFile / ".history"
      if (!history.isFile)
        sys.error(s"The shell history was removed by the cache purge. (${history.getAbsolutePath})")
      else {
        val actual = IO.read(history)
        if (actual != HistoryContent)
          sys.error(
            s"The shell history content was changed by the cache purge. (${history.getAbsolutePath})\nexpected: $HistoryContent\nactual:   $actual"
          )
        else ()
      }
    },
  )
