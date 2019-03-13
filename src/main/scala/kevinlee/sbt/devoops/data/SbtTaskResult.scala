package kevinlee.sbt.devoops.data

import kevinlee.git.GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  final case class GitCommandTaskResult(gitCommandResult: Seq[GitCommandResult]) extends SbtTaskResult

  final case class TaskResult(result: Seq[String]) extends SbtTaskResult

  def gitCommandTaskResult(gitCommandResult: Seq[GitCommandResult]): SbtTaskResult =
    GitCommandTaskResult(gitCommandResult)

  def taskResult(result: Seq[String]): SbtTaskResult =
    TaskResult(result)

  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult(gitCommandResults) =>
      val delimiter = ">> "
      s"""
         |task success> git commands
         |${gitCommandResults.map(GitCommandResult.render).mkString(delimiter, s"\n$delimiter", "")}
         |""".stripMargin

    case TaskResult(result) =>
      val delimiter = ">> "
      s"""
         |task success> GitHub task
         |${result.mkString(delimiter, s"\n$delimiter", "")}
         |""".stripMargin
  }

  def consolePrintln(sbtTaskResult: SbtTaskResult): Unit =
    println(render(sbtTaskResult))

}
