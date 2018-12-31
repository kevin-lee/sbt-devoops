package io.kevinlee.sbt

import io.kevinlee.semver.{Major, Minor, SemanticVersion}

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

}
