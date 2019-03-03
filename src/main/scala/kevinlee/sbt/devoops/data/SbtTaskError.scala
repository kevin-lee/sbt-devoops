package kevinlee.sbt.devoops.data

import kevinlee.git.GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-05
  */
sealed trait SbtTaskError

object SbtTaskError {

  // $COVERAGE-OFF$
  final case class GitCommandTaskError(cause: GitCommandError) extends SbtTaskError
  final case class GitTaskError(cause: String) extends SbtTaskError
  final case class NoFileFound(name: String, filePaths: List[String]) extends SbtTaskError

  def gitCommandTaskError(cause: GitCommandError): SbtTaskError =
    GitCommandTaskError(cause)

  def gitTaskError(cause: String): SbtTaskError =
    GitTaskError(cause)

  def noFileFound(name: String, filePaths: List[String]): SbtTaskError =
    NoFileFound(name, filePaths)

  def render(sbtTaskError: SbtTaskError): String = sbtTaskError match {

    case GitCommandTaskError(err: GitCommandError) =>
      GitCommandError.render(err)

    case GitTaskError(cause) =>
      s"task failed> git command: $cause"

    case NoFileFound(name: String, filePaths) =>
      s"No file found for $name. Expected files: ${filePaths.mkString("[", ",", "]")}"

  }

  def error(sbtTaskError: SbtTaskError): Nothing =
    sys.error(render(sbtTaskError))

}
