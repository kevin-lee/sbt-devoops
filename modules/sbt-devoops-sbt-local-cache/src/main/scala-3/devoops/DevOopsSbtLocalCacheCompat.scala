package devoops

import sbt.*
import sbt.Keys.*
import sbt.internal.RemoteCache
import sbtcompat.PluginCompat.*

/** sbt 2 implementation of the build-local cache support.
  *
  * sbt 2 caches every task result in a machine-wide, content-addressed disk cache (e.g.
  * `~/Library/Caches/sbt/v2` on macOS) which `clean` does not touch. This object provides a way to
  * redirect that cache into the build itself so that it can be removed without affecting any other
  * project on the machine.
  *
  * @author Kevin Lee
  * @since 2026-07-15
  */
private[devoops] object DevOopsSbtLocalCacheCompat {

  val CleanLocalCacheCommandName: String = "devOopsCleanLocalCache"

  private val cacheDeletionLock: AnyRef = new AnyRef

  /** The cache directory is only safe to delete when it lives inside the build. Deleting the
    * machine-wide cache would affect every other project on the machine, which is exactly what this
    * feature is meant to avoid.
    */
  private def isBuildLocal(cacheDir: File, baseDir: File): Boolean =
    cacheDir.getAbsoluteFile.toPath.normalize.startsWith(baseDir.getAbsoluteFile.toPath.normalize)

  private def humanReadableSize(bytes: Long): String = {
    val kib = 1024d
    val mib = kib * 1024d
    val gib = mib * 1024d
    if (bytes >= gib) f"${bytes / gib}%.1f GiB"
    else if (bytes >= mib) f"${bytes / mib}%.1f MiB"
    else if (bytes >= kib) f"${bytes / kib}%.1f KiB"
    else s"$bytes B"
  }

  private def totalSizeOf(dir: File): Long =
    if (dir.exists) (dir.allPaths --- dir.allPaths.filter(_.isDirectory)).get().map(_.length()).sum
    else 0L

  /** The sbt 2 shell command history, written to `<rootOutputDirectory>/.history`. It lives inside
    * the output directory but is not cache content, so it must survive a cache purge.
    */
  private def historyFileName: String = ".history"

  /** Deletes the build-local cache directory and the contents of the sbt 2 root output directory,
    * then recreates the empty `cas` and `ac` directories so the `DiskActionCacheStore` of the running
    * session keeps working.
    *
    * The shell command history (`<outDir>/.history`) is preserved since it is not cache content.
    */
  private def deleteBuildLocalCache(cacheDir: File, outDir: File): Unit =
    cacheDeletionLock.synchronized {
      IO.delete(cacheDir)
      deleteOutputDirKeepingHistory(outDir)
      IO.createDirectory(cacheDir / "cas")
      IO.createDirectory(cacheDir / "ac")
    }

  /** Deletes everything under `outDir` except the shell command history file, so removing the cache
    * does not wipe the user's command history.
    *
    * The history file is a direct child of `outDir` (`<outDir>/.history`), so only the top level
    * needs filtering: every other child is deleted wholesale and the history file itself is never
    * touched. Unlike a backup-and-restore, there is no window where the history exists only in
    * memory, and `outDir` itself is kept.
    */
  private def deleteOutputDirKeepingHistory(outDir: File): Unit =
    Option(outDir.listFiles).foreach { files =>
      IO.delete(files.filter(_.getName != historyFileName).toSeq)
    }

  private def notBuildLocalWarning(cacheDir: File, baseDir: File): String =
    s"""The sbt local cache is not build-local so it was not removed.
       |  cache directory: ${cacheDir.getAbsolutePath}
       |  build directory: ${baseDir.getAbsolutePath}
       |This cache is shared with all the other builds on this machine, so removing it would affect them.
       |To make it build-local, set `Global / devOopsUseLocalCache := true` in build.sbt.
       |To remove the machine-wide cache instead, use sbt's built-in `cleanFull` command.""".stripMargin

  /** When `useLocalCache` is `false`, this resolves to `RemoteCache.defaultCacheLocation`, the exact
    * value sbt 2 itself assigns to `localCacheDirectory`, so it is equivalent to leaving the setting
    * untouched.
    */
  def buildLocalCacheRedirectSettings(
    useLocalCache: SettingKey[Boolean],
    cacheDir: SettingKey[File],
  ): Seq[Def.Setting[_]] = Seq(
    Global / localCacheDirectory := {
      if ((Global / useLocalCache).value) (Global / cacheDir).value
      else RemoteCache.defaultCacheLocation
    }
  )

  def cleanLocalCacheCommand: Command =
    Command.command(
      CleanLocalCacheCommandName,
      "Remove the build-local sbt 2 cache.",
      "Remove the build-local sbt 2 cache directory (sbt 2 only). " +
        "It does nothing if the cache is not build-local (i.e. `devOopsUseLocalCache` is not `true`).",
    ) { state =>
      val extracted = Project.extract(state)
      val cacheDir  = extracted.get(Global / localCacheDirectory)
      val baseDir   = extracted.get(ThisBuild / baseDirectory)
      val outDir    = extracted.get(Global / rootOutputDirectory).toFile
      val log       = state.log
      if (isBuildLocal(cacheDir, baseDir)) {
        deleteBuildLocalCache(cacheDir, outDir)
        log.info(s"The build-local sbt cache has been removed. (${cacheDir.getAbsolutePath})")
        "clearCaches" :: state
      } else {
        log.warn(notBuildLocalWarning(cacheDir, baseDir))
        state
      }
    }

  def localCacheSizeSettings(key: TaskKey[Long]): Seq[Def.Setting[_]] = Seq(
    Global / key := Def.uncached {
      val cacheDir = (Global / localCacheDirectory).value
      val size     = totalSizeOf(cacheDir)
      streams.value.log.info(s"sbt local cache: ${cacheDir.getAbsolutePath} (${humanReadableSize(size)})")
      size
    }
  )

  private def purgeBuildLocalCacheTask: Def.Initialize[Task[Unit]] = Def.task {
    val cacheDir = (Global / localCacheDirectory).value
    val baseDir  = (ThisBuild / baseDirectory).value
    val outDir   = (Global / rootOutputDirectory).value.toFile
    val log      = streams.value.log
    if (isBuildLocal(cacheDir, baseDir)) {
      deleteBuildLocalCache(cacheDir, outDir)
      log.info(s"The build-local sbt cache has been removed. (${cacheDir.getAbsolutePath})")
    } else {
      log.warn(notBuildLocalWarning(cacheDir, baseDir))
    }
  }

  def cleanIncludesLocalCacheSettings(flag: SettingKey[Boolean]): Seq[Def.Setting[_]] = Seq(
    clean := Def.uncached {
      Def.taskDyn {
        val cleaned = clean.value
        if ((Global / flag).value) Def.task { purgeBuildLocalCacheTask.value; cleaned }
        else Def.task(cleaned)
      }.value
    }
  )

}
