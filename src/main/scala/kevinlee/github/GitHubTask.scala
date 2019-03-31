package kevinlee.github

import kevinlee.fp.EitherT
import kevinlee.fp.Writer.Writer
import kevinlee.git.Git.GitCmdHistoryWriter
import kevinlee.git.{GitCmdAndResult, GitCommandError}
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-03-31
  */
object GitHubTask {

  type GitHubTaskHistoryWriter[A] = Writer[List[String], A]

  def fromGitTask[A](
    taskResult: EitherT[GitCmdHistoryWriter, GitCommandError, A]
  ): EitherT[GitHubTaskHistoryWriter, GitHubError, A] =
    EitherT[GitHubTaskHistoryWriter, GitHubError, A](
      taskResult.leftMap(GitHubError.causedByGitCommandError)
        .run
        .mapWritten(_.map(GitCmdAndResult.render))
    )
}
