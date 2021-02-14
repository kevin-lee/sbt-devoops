package kevinlee.sbt.devoops

import sbt.{AutoPlugin, PluginTrigger, Plugins}

/** @author Kevin Lee
  * @since 2019-01-01
  */
@deprecated(
  message =
    "DevOopsGitReleasePlugin is deprecated. Please use DevOopsGitHubReleasePlugin instead.",
  since = "2.0.0",
)
object DevOopsGitReleasePlugin extends AutoPlugin {
  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = noTrigger
}
