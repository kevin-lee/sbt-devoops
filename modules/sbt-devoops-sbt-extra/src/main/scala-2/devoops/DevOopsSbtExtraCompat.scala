package devoops

import sbt._

/** sbt 1 (no-op) implementation of "always show the welcome message on an interactive start".
  *
  * The problem this feature solves does not exist on sbt 1. sbt 1 has no thin client, so starting
  * `sbt` always loads the project, which always prints `onLoadMessage`. It is only sbt 2's `sbtn`
  * client attaching to an already running server that skips the load and so skips the message.
  *
  * Every member here exists only so that the plugin can be built from the same sources for sbt 1
  * and sbt 2. See the scala-3 twin of this object for the real implementation.
  *
  * @author Kevin Lee
  * @since 2026-07-20
  */
private[devoops] object DevOopsSbtExtraCompat {

  def alwaysShowOnLoadMessageSettings(flag: SettingKey[Boolean]): Seq[Def.Setting[_]] = {
    val _ = flag
    Seq.empty
  }

}
