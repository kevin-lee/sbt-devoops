package kevinlee.sbt.devoops.data

import cats.data._
import kevinlee.git.{GitCmd, GitCmdAndResult, GitCommandResult}

/** @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  type SbtTaskHistory = List[SbtTaskResult]

  type SbtTaskHistoryWriter[F[_], A] = WriterT[F, SbtTaskHistory, A]

  final case class GitCommandTaskResult(gitCmdAndResult: GitCmdAndResult) extends SbtTaskResult

  final case class GitHubTaskResult(gitHubTaskResult: String) extends SbtTaskResult

  final case class TaskResult(result: Seq[String]) extends SbtTaskResult

  final case class SbtTaskResults(sbtTaskResults: List[SbtTaskResult]) extends SbtTaskResult

  final case class NonSbtTaskResult(result: String) extends SbtTaskResult

  def gitCommandTaskResult(gitCmdAndResult: GitCmdAndResult): SbtTaskResult =
    GitCommandTaskResult(gitCmdAndResult)

  def gitHubTaskResult(gitHubTaskResult: String): SbtTaskResult =
    GitHubTaskResult(gitHubTaskResult)

  def taskResult(result: Seq[String]): SbtTaskResult =
    TaskResult(result)

  def sbtTaskResults(sbtTaskResults: List[SbtTaskResult]): SbtTaskResult =
    SbtTaskResults(sbtTaskResults)

  def nonSbtTaskResult(result: String): SbtTaskResult =
    NonSbtTaskResult(result)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult(GitCmdAndResult(gitCmd, gitCommandResult)) =>
      s"${GitCmd.render(gitCmd)}${GitCommandResult.render(gitCommandResult)}"

    case TaskResult(result) =>
      if (result.isEmpty) {
        ""
      } else {
        val delimiter = ">> "
        s"""
           |task success> GitHub task
           |${result.mkString(delimiter, s"\n$delimiter", "")}
           |""".stripMargin
      }

    case GitHubTaskResult(result) =>
      s"""task success>
         |>> $result
         |""".stripMargin

    case SbtTaskResults(results) =>
      if (results.isEmpty) {
        ""
      } else {
        val delimiter = ">> "
        s"""task success>
           |${results.map(render).mkString(delimiter, s"\n$delimiter", "")}
           |""".stripMargin
      }

    case NonSbtTaskResult(result) =>
      s"""non sbt task success> $result""".stripMargin

  }

  def consolePrintln(sbtTaskResult: SbtTaskResult): Unit =
    println(render(sbtTaskResult))

}
