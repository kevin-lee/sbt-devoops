package devoops

import sbt.*
import sbt.Keys.*

/** Per-axis settings that have no sbt 2 counterpart.
  *
  * sbt 1 resolves with Ivy, which can fail the build on circular dependencies. sbt 2 made
  * Coursier mandatory and removed `CircularDependencyLevel` from the public API, so the
  * scala-3 twin of this object provides no settings.
  *
  * @author Kevin Lee
  * @since 2026-07-04
  */
private[devoops] object DevOopsScalaPluginCompat {

  val circularDependencyCheckSettings: Seq[Setting[_]] = Seq(
    updateOptions := updateOptions.value.withCircularDependencyLevel(CircularDependencyLevel.Error)
  )

}
