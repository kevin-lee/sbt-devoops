package devoops

import sbt.{Def, *}

/** @author Kevin Lee
  * @since 2026-07-15
  */
object DevOopsSbtLocalCachePlugin extends AutoPlugin {

  override def requires: Plugins      = plugins.JvmPlugin
  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val devOopsBuildLocalCacheDirectory: SettingKey[File] = settingKey[File](
      "sbt 2 only: The build-local sbt cache directory used when devOopsUseLocalCache is true. " +
        "(default: <build root>/.sbt-local-cache)"
    )

    val devOopsUseLocalCache: SettingKey[Boolean] = settingKey[Boolean](
      "sbt 2 only: If true, use a build-local sbt cache (devOopsBuildLocalCacheDirectory) instead of the machine-wide one. " +
        "Set it at the Global scope: Global / devOopsUseLocalCache := true (default: false)"
    )

    val devOopsCleanIncludesLocalCache: SettingKey[Boolean] = settingKey[Boolean](
      "sbt 2 only: If true, `clean` also removes the build-local cache in every project. " +
        "It has no effect when the cache is not build-local. (default: false)"
    )

    val devOopsLocalCacheSize: TaskKey[Long] = taskKey[Long](
      "sbt 2 only: Show the location and the total size of the sbt local cache in use, and return the size in bytes."
    )
  }

  import autoImport.*

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    devOopsBuildLocalCacheDirectory := (ThisBuild / Keys.baseDirectory).value / ".sbt-local-cache",
    devOopsUseLocalCache := false,
    devOopsCleanIncludesLocalCache := false,
    Keys.commands += DevOopsSbtLocalCacheCompat.cleanLocalCacheCommand,
  ) ++ DevOopsSbtLocalCacheCompat.localCacheSizeSettings(devOopsLocalCacheSize) ++
    DevOopsSbtLocalCacheCompat.buildLocalCacheRedirectSettings(devOopsUseLocalCache, devOopsBuildLocalCacheDirectory)

  /** Because this plugin `requires = plugins.JvmPlugin`, `Plugins.topologicalSort` orders these
    * `projectSettings` AFTER `JvmPlugin.projectSettings` (which defines sbt's default `clean`), so
    * this override wins in every project without needing an explicit `.settings(...)` add.
    */
  override def projectSettings: Seq[Def.Setting[_]] =
    DevOopsSbtLocalCacheCompat.cleanIncludesLocalCacheSettings(devOopsCleanIncludesLocalCache)

}
