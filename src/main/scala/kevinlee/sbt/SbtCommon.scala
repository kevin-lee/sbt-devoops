package kevinlee.sbt

import just.semver.SemVer
import just.semver.SemVer.{Major, Minor}

import sbt.MessageOnlyException

/**
  * @author Kevin Lee
  * @since 2018-12-28
  */
object SbtCommon {

  def crossVersionProps[T](
    commonProps: Seq[T]
  , version: SemVer)(
    versionSpecific: PartialFunction[(Major, Minor), Seq[T]]
  ): Seq[T] =
    commonProps ++ versionSpecific((version.major, version.minor))

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def messageOnlyException(message: String): Nothing =
    throw new MessageOnlyException(message)
}
