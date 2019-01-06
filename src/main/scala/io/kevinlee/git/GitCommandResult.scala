package io.kevinlee.git

sealed trait GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandResult {
  // $COVERAGE-OFF$

  final case class GitCurrentBranchName(name: Git.BranchName) extends GitCommandResult
  final case class GitCheckoutResult(result: String) extends GitCommandResult
  final case class GitTagResult(result: String) extends GitCommandResult

  def gitCurrentBranchName(name: Git.BranchName): GitCommandResult =
    GitCurrentBranchName(name)

  def gitCheckoutResult(result: String): GitCommandResult =
    GitCheckoutResult(result)

  def gitTagResult(result: String): GitCommandResult =
    GitTagResult(result)

  def render(gitCommandResult: GitCommandResult): String = gitCommandResult match {

    case GitCurrentBranchName(Git.BranchName(name)) =>
      s"git current branch name: $name"

    case GitCheckoutResult(result) =>
      s"git checkout has finished: $result"

    case GitTagResult(result) =>
      s"git tag has finished: $result"

  }

  // $COVERAGE-ON$
}