package kevinlee.sbt

import kevinlee.semver.{Major, Minor, SemanticVersion}
import sbt.MessageOnlyException

/**
  * @author Kevin Lee
  * @since 2018-12-28
  */
object SbtCommon {

  def crossVersionProps[T](
    commonProps: Seq[T]
  , version: SemanticVersion)(
    versionSpecific: PartialFunction[(Major, Minor), Seq[T]]
  ): Seq[T] =
    commonProps ++ versionSpecific((version.major, version.minor))

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def messageOnlyException(message: String): Nothing =
    throw new MessageOnlyException(message)
}
