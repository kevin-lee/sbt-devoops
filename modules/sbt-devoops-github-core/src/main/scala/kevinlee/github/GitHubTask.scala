package kevinlee.github

import cats.*
import cats.data.*
import kevinlee.git.{Git, GitCmdAndResult}
import kevinlee.github.GitHubTask.GitHubTaskResult
import kevinlee.github.data.GitHubError

/** @author Kevin Lee
  * @since 2019-03-31
  */
trait GitHubTask[F[_]] {
  def fromGitTask[A](
    taskResult: Git.CmdResult[F, A]
  ): GitHubTaskResult[F, A]

  def toLeftIfNone[A](
    maybeA: Option[A],
    whenNone: => GitHubError
  ): GitHubTaskResult[F, A]
}

object GitHubTask {

  type GitHubTaskHistoryWriter[F[_], A] = WriterT[F, List[String], A]

  type GitHubTaskResult[F[_], A] = EitherT[GitHubTaskHistoryWriter[F, *], GitHubError, A]

  def apply[F[_]: GitHubTask]: GitHubTask[F] = implicitly[GitHubTask[F]]

  implicit def gitHubTaskF[F[_]: Monad]: GitHubTask[F] = new GitHubTaskF[F]

  final class GitHubTaskF[F[_]: Monad] extends GitHubTask[F] {
    def fromGitTask[A](
      taskResult: Git.CmdResult[F, A]
    ): GitHubTaskResult[F, A] =
      EitherT[GitHubTaskHistoryWriter[F, *], GitHubError, A](
        taskResult
          .leftMap(GitHubError.causedByGitCommandError)
          .value
          .mapWritten(_.map(GitCmdAndResult.render))
      )

    override def toLeftIfNone[A](
      maybeA: Option[A],
      whenNone: => GitHubError
    ): GitHubTaskResult[F, A] =
      EitherT[GitHubTaskHistoryWriter[F, *], GitHubError, A](
        WriterT(
          Monad[F].pure(
            (
              List.empty[String],
              maybeA.toRight[GitHubError](whenNone)
            )
          )
        )
      )

  }
}
