package kevinlee.sbt.devoops.data

import kevinlee.fp.EitherOps._
import kevinlee.fp.{EitherT, Writer}
import kevinlee.git.Git.GitCmdHistoryWriter
import kevinlee.git.GitCommandError
import kevinlee.github.data.GitHubError
import SbtTaskResult.{SbtTaskHistory, SbtTaskHistoryWriter}

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
object SbtTask {
  // $COVERAGE-OFF$

  def fromGitTask[A](
    taskResult: EitherT[GitCmdHistoryWriter, GitCommandError, A]
  ): EitherT[SbtTaskHistoryWriter, SbtTaskError, A] =
    EitherT[SbtTaskHistoryWriter, SbtTaskError, A](
      taskResult.leftMap(SbtTaskError.gitCommandTaskError)
        .run
        .mapWritten(_.map(SbtTaskResult.gitCommandTaskResult))
    )

  def toLeftWhen[C](condition: => Boolean, whenFalse: => C): EitherT[SbtTaskHistoryWriter, C, Unit] = EitherT[SbtTaskHistoryWriter, C, Unit](
    Writer(
      List.empty[SbtTaskResult]
    , if (condition) Left(whenFalse).castR[Unit] else Right(()).castL[C]
    )
  )

  def handleSbtTask(
    sbtTaskResult: (SbtTaskHistory, Either[SbtTaskError, Unit])
  ): Unit =
    sbtTaskResult match {
      case (history, Left(error)) =>
        println(
          s"""${SbtTaskError.render(error)}
             |>> sbt task failed after succeeding the following tasks
             |${SbtTaskResult.sbtTaskResults(history)}
             |""".stripMargin
        )
        SbtTaskError.error(error)
      case (history, Right(())) =>
        SbtTaskResult.consolePrintln(
          SbtTaskResult.sbtTaskResults(history)
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
