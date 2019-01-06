package io.kevinlee.sbt.devoops.data

import io.kevinlee.git.GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-05
  */
sealed trait SbtTaskError

object SbtTaskError {

  // $COVERAGE-OFF$
  final case class GitTaskGitCommandError(cause: GitCommandError) extends SbtTaskError
  final case class GitTaskError(cause: String) extends SbtTaskError

  def gitTaskGitCommandError(cause: GitCommandError): SbtTaskError =
    GitTaskGitCommandError(cause)

  def gitTaskError(cause: String): SbtTaskError =
    GitTaskError(cause)

  def render(sbtTaskError: SbtTaskError): String = sbtTaskError match {

    case GitTaskGitCommandError(err: GitCommandError) =>
      GitCommandError.render(err)

    case GitTaskError(cause) =>
      s"git task has failed: $cause"

  }

}
