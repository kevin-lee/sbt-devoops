package io.kevinlee.git

sealed trait GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandError {
  // $COVERAGE-OFF$

  final case class GitUnexpectedCommandResultError(gitCommandResult: GitCommandResult, expectedResult: String) extends GitCommandError
  final case class GitCurrentBranchError(code: Int, error: String) extends GitCommandError
  final case class GitCheckoutError(code: Int, error: String) extends GitCommandError
  final case class GitFetchError(code: Int, error: String, arg: Option[String]) extends GitCommandError
  final case class GitTagError(code: Int, error: String) extends GitCommandError

  def gitUnexpectedCommandResultError(gitCommandResult: GitCommandResult, expectedResult: String): GitCommandError =
    GitUnexpectedCommandResultError(gitCommandResult, expectedResult)

  def gitCurrentBranchError(code: Int, error: String): GitCommandError =
    GitCurrentBranchError(code, error)

  def gitCheckoutError(code: Int, error: String): GitCommandError =
    GitCheckoutError(code, error)

  def gitFetchError(code: Int, error: String, arg: Option[String]): GitCommandError =
    GitFetchError(code, error, arg)

  def gitTagError(code: Int, error: String): GitCommandError =
    GitTagError(code, error)

  private def renderCodeAndError(code: Int, error: String): String =
    s"[code: $code], [error: $error]"

  def render(gitError: GitCommandError): String = gitError match {
    case GitUnexpectedCommandResultError(gitCommandResult, expectedResult) =>
      s"""Unexpected git command result
         |expected: $expectedResult
         |---
         |  actual: ${GitCommandResult.render(gitCommandResult)}
         |""".stripMargin

    case GitCurrentBranchError(code, error) =>
      s"Error] Git getting the current branch: ${renderCodeAndError(code, error)}"

    case GitCheckoutError(code, error) =>
      s"Error] Git checkout: ${renderCodeAndError(code, error)}"

    case GitFetchError(code, error, arg) =>
      s"Error] Git fetch${arg.fold("")(a => s" $a")}: ${renderCodeAndError(code, error)}"

    case GitTagError(code, error) =>
      s"Error] Git tag: ${renderCodeAndError(code, error)}"
  }

  // $COVERAGE-ON$
}