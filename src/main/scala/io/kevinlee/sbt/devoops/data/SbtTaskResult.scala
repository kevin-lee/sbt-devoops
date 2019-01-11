package io.kevinlee.sbt.devoops.data

import io.kevinlee.git.GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  final case class GitCommandTaskResult(gitCommandResult: Seq[GitCommandResult]) extends SbtTaskResult

  def gitCommandTaskResult(gitCommandResult: Seq[GitCommandResult]): SbtTaskResult =
    GitCommandTaskResult(gitCommandResult)

  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult(gitCommandResults) =>
      val delimiter = "  >> "
      s"""
         |task success> git commands
         |${gitCommandResults.map(GitCommandResult.render).mkString(delimiter, s"\n$delimiter", "")}
         |""".stripMargin
  }

  def consolePrintln(sbtTaskResult: SbtTaskResult): Unit =
    println(render(sbtTaskResult))

}
