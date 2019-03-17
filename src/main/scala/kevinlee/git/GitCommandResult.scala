package kevinlee.git

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
sealed trait GitCommandResult

object GitCommandResult {
  // $COVERAGE-OFF$

  final case class GenericResult(result: List[String]) extends GitCommandResult

  def genericResult(result: List[String]): GitCommandResult =
    GenericResult(result)

  def render(gitCommandResult: GitCommandResult): String = gitCommandResult match {
    case GenericResult(result) =>
      result.mkString("[", ", ", "]")
  }

  // $COVERAGE-ON$
}