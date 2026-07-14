package devoops

import sbt._
import sbt.Keys._
import sbtcompat.PluginCompat._

/** sbt 1 (no-op) implementation of the build-local cache support.
  *
  * sbt 1 has no machine-wide task result cache, so there is nothing to redirect or remove. Every
  * member here exists only so that the plugin can be built from the same sources for sbt 1 and sbt
  * 2.
  *
  * @author Kevin Lee
  * @since 2026-07-15
  */
private[devoops] object DevOopsSbtLocalCacheCompat {

  val CleanLocalCacheCommandName: String = "devOopsCleanLocalCache"

  private val Sbt2OnlyMessage: String =
    "The build-local cache feature is for sbt 2 only, and it does nothing on sbt 1 which has no machine-wide task cache."

  def buildLocalCacheRedirectSettings(
    useLocalCache: SettingKey[Boolean],
    cacheDir: SettingKey[File],
  ): Seq[Def.Setting[_]] = {
    val _ = (useLocalCache, cacheDir)
    Seq.empty
  }

  def cleanLocalCacheCommand: Command =
    Command.command(
      CleanLocalCacheCommandName,
      "Remove the build-local sbt 2 cache.",
      "Remove the build-local sbt 2 cache directory (sbt 2 only). It does nothing on sbt 1.",
    ) { state =>
      state.log.warn(Sbt2OnlyMessage)
      state
    }

  def localCacheSizeSettings(key: TaskKey[Long]): Seq[Def.Setting[_]] = Seq(
    Global / key := Def.uncached {
      streams.value.log.warn(Sbt2OnlyMessage)
      0L
    }
  )

  def cleanIncludesLocalCacheSettings(flag: SettingKey[Boolean]): Seq[Def.Setting[_]] = {
    val _ = flag
    Seq.empty
  }

}
