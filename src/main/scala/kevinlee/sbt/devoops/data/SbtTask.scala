package kevinlee.sbt.devoops.data

import kevinlee.git.{GitCommandError, GitCommandResult}
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
object SbtTask {
  // $COVERAGE-OFF$

  def handleGitCommandTask(gitCommandTaskResult: Either[GitCommandError, Seq[GitCommandResult]]): Unit =
    gitCommandTaskResult
      .left.map(SbtTaskError.gitCommandTaskError)
      .right.map(SbtTaskResult.gitCommandTaskResult)
      .fold(
        SbtTaskError.error
      , SbtTaskResult.consolePrintln
      )

  def handleGitHubTask(gitHubTaskResult: Either[GitHubError, Seq[String]]): Unit =
    gitHubTaskResult
      .left.map(SbtTaskError.gitHubTaskError)
      .right.map(SbtTaskResult.taskResult)
      .fold(
        SbtTaskError.error
      , SbtTaskResult.consolePrintln
      )

}
