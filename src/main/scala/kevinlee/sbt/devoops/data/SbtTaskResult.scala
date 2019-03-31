package kevinlee.sbt.devoops.data

import kevinlee.fp.Writer.Writer
import kevinlee.git.Git.GitCmdAndResult
import kevinlee.git.{GitCmd, GitCommandResult}

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  type SbtTaskHistory = List[SbtTaskResult]

  type SbtTaskHistoryWriter[A] = Writer[SbtTaskHistory, A]

  final case class GitCommandTaskResult(gitCmdAndResult: GitCmdAndResult) extends SbtTaskResult

  final case class TaskResult(result: Seq[String]) extends SbtTaskResult

  final case class SbtTaskResults(sbtTaskResults: List[SbtTaskResult]) extends SbtTaskResult

  def gitCommandTaskResult(gitCmdAndResult: GitCmdAndResult): SbtTaskResult =
    GitCommandTaskResult(gitCmdAndResult)

  def taskResult(result: Seq[String]): SbtTaskResult =
    TaskResult(result)

  def sbtTaskResults(sbtTaskResults: List[SbtTaskResult]): SbtTaskResult =
    SbtTaskResults(sbtTaskResults)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult(GitCmdAndResult(gitCmd, gitCommandResult)) =>
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
