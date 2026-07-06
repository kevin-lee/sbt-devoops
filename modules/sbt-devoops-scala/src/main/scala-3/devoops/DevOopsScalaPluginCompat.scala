package devoops

import sbt.*

/** Per-axis settings that have no sbt 2 counterpart.
  *
  * sbt 2 made Coursier mandatory and removed `CircularDependencyLevel` from the public API,
  * so the Ivy circular-dependency check configured on the sbt 1 axis simply does not exist
  * here. See the scala-2 twin of this object.
  *
  * @author Kevin Lee
  * @since 2026-07-04
  */
private[devoops] object DevOopsScalaPluginCompat {

  val circularDependencyCheckSettings: Seq[Setting[_]] = Seq.empty

}
