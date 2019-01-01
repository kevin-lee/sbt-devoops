package io.kevinlee.git

sealed trait GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandError {
  // $COVERAGE-OFF$

  final case class GitTagError(code: Int, error: String) extends GitCommandError
  final case class GitCheckoutError(code: Int, error: String) extends GitCommandError

  def gitTagError(code: Int, error: String): GitCommandError =
    GitTagError(code, error)

  def gitCheckoutError(code: Int, error: String): GitCommandError =
    GitCheckoutError(code, error)

  def render(gitError: GitCommandError): String = gitError match {
    case GitTagError(code, error) =>
      s"failed] Git tag: [code: $code], [error: $error]"

    case GitCheckoutError(code, error) =>
      s"failed] Git checkout: [code: $code], [error: $error]"
  }

  // $COVERAGE-ON$
}