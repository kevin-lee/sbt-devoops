package kevinlee.sbt

import just.semver.SemVer
import sbt.MessageOnlyException

/** @author Kevin Lee
  * @since 2018-12-28
  */
object SbtCommon {

  def crossVersionProps[T](commonProps: Seq[T], version: SemVer)(
    versionSpecific: PartialFunction[(SemVer.Major, SemVer.Minor, SemVer.Patch), Seq[T]]
  ): Seq[T] =
    commonProps ++ versionSpecific((version.major, version.minor, version.patch))

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def messageOnlyException(message: String): Nothing =
    throw new MessageOnlyException(message)
}
