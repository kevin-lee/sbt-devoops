package kevinlee.sbt.devoops.data

import kevinlee.fp._, Writer.Writer
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
    taskResult: => EitherT[GitCmdHistoryWriter, GitCommandError, A]
  ): EitherT[SbtTaskHistoryWriter, SbtTaskError, A] =
    EitherT[SbtTaskHistoryWriter, SbtTaskError, A](
      taskResult.leftMap(SbtTaskError.gitCommandTaskError)
        .run
        .mapWritten(_.map(SbtTaskResult.gitCommandTaskResult))
    )

  def toLeftWhen[A](condition: => Boolean, whenFalse: => A): EitherT[SbtTaskHistoryWriter, A, Unit] = EitherT[SbtTaskHistoryWriter, A, Unit] {
    val aOrB: Either[A, Unit] = if (condition) Left(whenFalse) else Right(())
    Writer(
      List.empty[SbtTaskResult]
    , aOrB
    )
  }

  type HistoryStrings[A] = Writer[List[String], A]

  def eitherTWithHistoryStrings[A, B](
    r: Either[A, B])(fw: B => List[String]
  ): EitherT[HistoryStrings, A, B] = EitherT[HistoryStrings, A, B] {
    val w = r match {
      case Left(a) =>
        List.empty[String]
      case Right(b) =>
        fw(b)
    }
    Writer(w, r)
  }


  def handleSbtTask(
    sbtTaskResult: (SbtTaskHistory, Either[SbtTaskError, Unit])
  ): Unit =
    sbtTaskResult match {
      case (history, Left(error)) =>
        println(
          s"""Failure]
             |>> sbt task failed after succeeding the following tasks
             |${SbtTaskResult.render(SbtTaskResult.sbtTaskResults(history))}
             |
             |${SbtTaskError.render(error)}
             |""".stripMargin
        )
        SbtTaskError.error(error)
      case (history, Right(())) =>
        SbtTaskResult.consolePrintln(
          SbtTaskResult.sbtTaskResults(history)
        )
    }

  def handleGitHubTask(gitHubTaskResult: (List[String], Either[GitHubError, Unit])): Unit =
    gitHubTaskResult match {
      case (history, Left(error)) =>
        val gitHubTaskError = SbtTaskError.gitHubTaskError(error)
        println(
          s"""Failure]
             |>> sbt task failed after succeeding the following tasks
             |${SbtTaskResult.render(SbtTaskResult.taskResult(history))}
             |
             |${SbtTaskError.render(gitHubTaskError)}
             |""".stripMargin
        )
        SbtTaskError.error(gitHubTaskError)
      case (history, Right(())) =>
        SbtTaskResult.consolePrintln(
          SbtTaskResult.taskResult(history)
        )
    }

}
