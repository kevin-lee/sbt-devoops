package kevinlee.git

sealed trait GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object GitCommandError {
  // $COVERAGE-OFF$

  final case class GenericGotCommandResultError(gitCmd: GitCmd, code: Int, errors: List[String]) extends GitCommandError

  def genericGotCommandResultError(gitCmd: GitCmd, code: Int, errors: List[String]): GitCommandError =
    GenericGotCommandResultError(gitCmd, code, errors)

  private def renderCodeAndError(gitCmd: GitCmd, code: Int, errors: List[String]): String =
    s"[cmd: ${GitCmd.render(gitCmd)}], [code: $code], [errors: ${errors.mkString("\n  ")}]"

  def render(gitError: GitCommandError): String = gitError match {
    case GenericGotCommandResultError(gitCmd, code, errors) =>
      renderCodeAndError(gitCmd, code, errors)
  }

  // $COVERAGE-ON$
}