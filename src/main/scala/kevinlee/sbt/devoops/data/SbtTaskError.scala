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

  def gitCommandTaskError(cause: GitCommandError): SbtTaskError =
    GitCommandTaskError(cause)

  def gitTaskError(cause: String): SbtTaskError =
    GitTaskError(cause)

  def render(sbtTaskError: SbtTaskError): String = sbtTaskError match {

    case GitCommandTaskError(err: GitCommandError) =>
      GitCommandError.render(err)

    case GitTaskError(cause) =>
      s"task failed> git command: $cause"

  }

  def error(sbtTaskError: SbtTaskError): Nothing =
    sys.error(render(sbtTaskError))

}
