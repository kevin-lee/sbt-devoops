package kevinlee.sbt.devoops.data

import kevinlee.git.{GitCmd, GitCommandError, GitCommandResult, SuccessHistory}
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-01-05
  */
sealed trait SbtTaskError

object SbtTaskError {

  // $COVERAGE-OFF$
  final case class GitCommandTaskError(successHistory: List[(GitCmd, GitCommandResult)], cause: GitCommandError) extends SbtTaskError
  final case class GitTaskError(cause: String) extends SbtTaskError
  final case class GitHubTaskError(cause: GitHubError) extends SbtTaskError
  final case class NoFileFound(name: String, filePaths: List[String]) extends SbtTaskError

  def gitCommandTaskError(
    successHistory: List[(GitCmd, GitCommandResult)]
  , cause: GitCommandError
  ): SbtTaskError =
    GitCommandTaskError(successHistory, cause)

  def gitTaskError(cause: String): SbtTaskError =
    GitTaskError(cause)

  def noFileFound(name: String, filePaths: List[String]): SbtTaskError =
    NoFileFound(name, filePaths)

  def gitHubTaskError(cause: GitHubError): SbtTaskError =
    GitHubTaskError(cause)

  def render(sbtTaskError: SbtTaskError): String = sbtTaskError match {

    case GitCommandTaskError(successHistory, err) =>
      s""">> ${GitCommandError.render(err)}
         |>> Git command failed after succeeding the following git commands
         |${SuccessHistory.render(successHistory)}
         |""".stripMargin

    case GitTaskError(cause) =>
      s"task failed> git command: $cause"

    case GitHubTaskError(cause) =>
      GitHubError.render(cause)

    case NoFileFound(name: String, filePaths) =>
      s"No file found for $name. Expected files: ${filePaths.mkString("[", ",", "]")}"

  }

  def error(sbtTaskError: SbtTaskError): Nothing =
    sys.error(render(sbtTaskError))

}
