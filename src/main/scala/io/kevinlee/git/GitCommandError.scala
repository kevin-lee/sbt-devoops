package io.kevinlee.git

import io.kevinlee.git.Git.{Repository, TagName}

sealed trait GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandError {
  // $COVERAGE-OFF$

  final case class GitUnexpectedCommandResultError(gitCommandResult: GitCommandResult, expectedResult: String) extends GitCommandError
  final case class GitCurrentBranchError(code: Int, errors: List[String]) extends GitCommandError
  final case class GitCheckoutError(code: Int, errors: List[String]) extends GitCommandError
  final case class GitFetchError(code: Int, errors: List[String], arg: Option[String]) extends GitCommandError
  final case class GitTagError(code: Int, errors: List[String]) extends GitCommandError
  final case class GitPushTagError(code: Int, errors: List[String], repository: Repository, tagName: TagName) extends GitCommandError

  def gitUnexpectedCommandResultError(gitCommandResult: GitCommandResult, expectedResult: String): GitCommandError =
    GitUnexpectedCommandResultError(gitCommandResult, expectedResult)

  def gitCurrentBranchError(code: Int, errors: List[String]): GitCommandError =
    GitCurrentBranchError(code, errors)

  def gitCheckoutError(code: Int, errors: List[String]): GitCommandError =
    GitCheckoutError(code, errors)

  def gitFetchError(code: Int, errors: List[String], arg: Option[String]): GitCommandError =
    GitFetchError(code, errors, arg)

  def gitTagError(code: Int, errors: List[String]): GitCommandError =
    GitTagError(code, errors)

  def gitPushTagError(code: Int, errors: List[String], repository: Repository, tagName: TagName): GitCommandError =
    GitPushTagError(code, errors, repository, tagName)

  private def renderCodeAndError(code: Int, errors: List[String]): String =
    s"[code: $code], [errors: ${errors.mkString("\n  ")}]"

  def render(gitError: GitCommandError): String = gitError match {
    case GitUnexpectedCommandResultError(gitCommandResult, expectedResult) =>
      s"""Unexpected git command result
         |expected: $expectedResult
         |---
         |  actual: ${GitCommandResult.render(gitCommandResult)}
         |""".stripMargin

    case GitCurrentBranchError(code, errors) =>
      s"Error] Git getting the current branch: ${renderCodeAndError(code, errors)}"

    case GitCheckoutError(code, errors) =>
      s"Error] Git checkout: ${renderCodeAndError(code, errors)}"

    case GitFetchError(code, errors, arg) =>
      s"Error] Git fetch${arg.fold("")(a => s" $a")}: ${renderCodeAndError(code, errors)}"

    case GitTagError(code, errors) =>
      s"Error] Git tag: ${renderCodeAndError(code, errors)}"

    case GitPushTagError(code, errors, repository, tagName) =>
      s"""Error] git push ${repository.value} ${tagName.value}
         |  code: $code
         |  errors:
         |  [
         |    ${errors.mkString("\n    ")}
         |  ]
         |""".stripMargin
  }

  // $COVERAGE-ON$
}