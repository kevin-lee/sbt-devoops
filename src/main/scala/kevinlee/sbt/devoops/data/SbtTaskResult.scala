package kevinlee.sbt.devoops.data

import kevinlee.git.{GitCmd, GitCommandResult}

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  final case class GitCommandTaskResult(gitCmdAndResult: (GitCmd, GitCommandResult)) extends SbtTaskResult

  final case class TaskResult(result: Seq[String]) extends SbtTaskResult

  final case class SbtTaskResults(sbtTaskResults: List[SbtTaskResult]) extends SbtTaskResult

  def gitCommandTaskResult(gitCmdAndResult: (GitCmd, GitCommandResult)): SbtTaskResult =
    GitCommandTaskResult(gitCmdAndResult)

  def taskResult(result: Seq[String]): SbtTaskResult =
    TaskResult(result)

  def sbtTaskResults(sbtTaskResults: List[SbtTaskResult]): SbtTaskResult =
    SbtTaskResults(sbtTaskResults)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult((gitCmd, gitCommandResult)) =>
       s"${GitCmd.render(gitCmd)}${GitCommandResult.render(gitCommandResult)}"

    case TaskResult(result) =>
      val delimiter = ">> "
      s"""
         |task success> GitHub task
         |${result.mkString(delimiter, s"\n$delimiter", "")}
         |""".stripMargin

    case SbtTaskResults(results) =>
      val delimiter = ">> "
      s"""task success>
         |${results.map(render).mkString(delimiter, s"\n$delimiter", "")}
         |""".stripMargin

  }

  def consolePrintln(sbtTaskResult: SbtTaskResult): Unit =
    println(render(sbtTaskResult))

}
