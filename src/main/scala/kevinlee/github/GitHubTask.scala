package kevinlee.github

import just.fp._
import kevinlee.git.{Git, GitCmdAndResult}
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-03-31
  */
object GitHubTask {

  type GitHubTaskHistoryWriter[A] = Writer[List[String], A]

  type GitHubTaskResult[A] = EitherT[GitHubTaskHistoryWriter, GitHubError, A]

  def fromGitTask[A](
    taskResult: Git.CmdResult[A]
  ): GitHubTaskResult[A] =
    EitherT[GitHubTaskHistoryWriter, GitHubError, A](
      taskResult.leftMap(GitHubError.causedByGitCommandError)
        .run
        .mapWritten(_.map(GitCmdAndResult.render))
    )
}
