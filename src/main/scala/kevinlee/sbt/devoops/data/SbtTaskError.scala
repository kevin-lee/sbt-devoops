package kevinlee.sbt.devoops.data

import kevinlee.git.GitCommandError
import kevinlee.github.data.GitHubError
import kevinlee.sbt.SbtCommon.messageOnlyException

import just.semver.{ParseError, SemVer}

/** @author Kevin Lee
  * @since 2019-01-05
  */
sealed trait SbtTaskError

object SbtTaskError {

  // $COVERAGE-OFF$
  final case class GitCommandTaskError(cause: GitCommandError)        extends SbtTaskError
  final case class GitTaskError(cause: String)                        extends SbtTaskError
  final case class GitHubTaskError(cause: GitHubError)                extends SbtTaskError
  final case class NoFileFound(name: String, filePaths: List[String]) extends SbtTaskError
  final case class SemVerFromProjectVersionParseError(projectVersion: String, parseError: ParseError)
      extends SbtTaskError
  final case class VersionNotEligibleForTagging(semVer: SemVer)       extends SbtTaskError
  final case class IoError(name: String, throwable: Throwable)        extends SbtTaskError

  def gitCommandTaskError(cause: GitCommandError): SbtTaskError =
    GitCommandTaskError(cause)

  def gitTaskError(cause: String): SbtTaskError =
    GitTaskError(cause)

  def noFileFound(name: String, filePaths: List[String]): SbtTaskError =
    NoFileFound(name, filePaths)

  def gitHubTaskError(cause: GitHubError): SbtTaskError =
    GitHubTaskError(cause)

  def semVerFromProjectVersionParseError(projectVersion: String, parseError: ParseError): SbtTaskError =
    SemVerFromProjectVersionParseError(projectVersion, parseError)

  def versionNotEligibleForTagging(semVer: SemVer): SbtTaskError =
    VersionNotEligibleForTagging(semVer)

  def ioError(name: String, throwable: Throwable): SbtTaskError =
    IoError(name, throwable)

  def render(sbtTaskError: SbtTaskError): String = sbtTaskError match {

    case GitCommandTaskError(err) =>
      s">> ${GitCommandError.render(err)}"

    case GitTaskError(cause) =>
      s"task failed> git command: $cause"

    case GitHubTaskError(cause) =>
      GitHubError.render(cause)

    case NoFileFound(name: String, filePaths) =>
      s"No file found for $name. Expected files: ${filePaths.mkString("[", ",", "]")}"

    case SemVerFromProjectVersionParseError(projectVersion, parseError) =>
      s"Parsing semantic version from project version failed. [projectVersion: $projectVersion, error: ${ParseError.render(parseError)}]"

    case VersionNotEligibleForTagging(semVer) =>
      s"""|  This version is not eligible for tagging. [version: ${SemVer.render(semVer)}]
          |  It should be GA version with any pre-release or meta-info suffix
          |    e.g.)
          |    * 1.0.0 (⭕️)
          |    * 1.0.0-SNAPSHOT (❌)
          |    * 1.0.0-beta (❌)
          |    * 1.0.0+123 (❌)
          |    * 1.0.0-beta+123 (❌)
          |""".stripMargin

    case IoError(name, throwable) =>
      s"""IO Error for $name
         |${throwable.getMessage}
         |""".stripMargin
  }

  /** Throws a MessageOnlyException after rendering the given SbtTaskError
    * @param sbtTaskError the given SbtTaskError to render
    * @tparam A This is only to make the compiler happy for the call-site.
    *           It doesn't mean anything since this method is throwing a MessageOnlyException
    * @return Nothing. It throws a MessageOnlyException.
    */
  def error[A](sbtTaskError: SbtTaskError): A =
    messageOnlyException(render(sbtTaskError))

}
