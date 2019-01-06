package io.kevinlee.sbt.devoops.data

import io.kevinlee.git.GitCommandResult

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
sealed trait SbtTaskResult

object SbtTaskResult {
  // $COVERAGE-OFF$

  final case class GitCommandTaskResult(gitCommandResult: GitCommandResult) extends SbtTaskResult

  def gitCommandTaskResult(gitCommandResult: GitCommandResult): SbtTaskResult =
    GitCommandTaskResult(gitCommandResult)

  def render(sbtTaskResult: SbtTaskResult): String = sbtTaskResult match {
    case GitCommandTaskResult(gitCommandResult) =>
      s"sbt task done: ${GitCommandResult.render(gitCommandResult)}"
  }

  def consolePrintln(sbtTaskResult: SbtTaskResult): Unit =
    println(render(sbtTaskResult))

}
