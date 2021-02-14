package kevinlee.sbt.devoops

import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, PluginTrigger, plugins}

/** @author Kevin Lee
  * @since 2018-12-30
  */
@deprecated(
  message = "kevinlee.sbt.devoops.DevOopsJavaPlugin is deprecated. Please use devoops.DevOopsJavaPlugin instead.",
  since = "2.0.0",
)
object DevOopsJavaPlugin extends AutoPlugin {
  // $COVERAGE-OFF$
  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = noTrigger
  // $COVERAGE-ON$
}
