package io.kevinlee.sbt.devoops.data

import io.kevinlee.git.{GitCommandError, GitCommandResult}

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
object SbtTask {
  // $COVERAGE-OFF$

  def handleGitCommandTask(gitCommandTaskResult: Either[GitCommandError, GitCommandResult]): Unit =
    gitCommandTaskResult
      .left.map(SbtTaskError.gitCommandTaskError)
      .right.map(SbtTaskResult.gitCommandTaskResult)
      .fold(
        SbtTaskError.error
      , SbtTaskResult.consolePrintln
      )

}
