package io.kevinlee.git

sealed trait GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandResult {
  // $COVERAGE-OFF$

  final case class GitTagResult(result: String) extends GitCommandResult
  final case class GitCheckoutResult(result: String) extends GitCommandResult

  def gitTagResult(result: String): GitCommandResult =
    GitTagResult(result)

  def gitCheckoutResult(result: String): GitCommandResult =
    GitCheckoutResult(result)

  def render(gitCommandResult: GitCommandResult): String = gitCommandResult match {
    case GitTagResult(result) =>
      s"git tag has finished: $result"

    case GitCheckoutResult(result) =>
      s"git checkout has finished: $result"
  }

  // $COVERAGE-ON$
}