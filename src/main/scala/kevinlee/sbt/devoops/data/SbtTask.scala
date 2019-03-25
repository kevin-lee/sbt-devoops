package kevinlee.sbt.devoops.data

import kevinlee.git.Git.GitCmdHistory
import kevinlee.git.GitCommandError
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
object SbtTask {
  // $COVERAGE-OFF$

  def handleGitCommandTask(
    gitCommandTaskResult: (GitCmdHistory, Either[GitCommandError, Unit])
  ): Unit =
    gitCommandTaskResult match {
      case (history, Left(error)) =>
        SbtTaskError.error(SbtTaskError.gitCommandTaskError(history, error))
      case (history, Right(())) =>
        SbtTaskResult.consolePrintln(
          SbtTaskResult.sbtTaskResults(history.map(SbtTaskResult.gitCommandTaskResult))
        )
    }

  def handleGitHubTask(gitHubTaskResult: Either[GitHubError, Seq[String]]): Unit =
    gitHubTaskResult
      .left.map(SbtTaskError.gitHubTaskError)
      .right.map(SbtTaskResult.taskResult)
      .fold(
        SbtTaskError.error
      , SbtTaskResult.consolePrintln
      )

}
