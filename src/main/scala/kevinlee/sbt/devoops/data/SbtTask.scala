package kevinlee.sbt.devoops.data

import SbtTaskResult.{SbtTaskHistory, SbtTaskHistoryWriter}

import kevinlee.fp.JustSyntax._
import kevinlee.fp._

import kevinlee.git.Git
import kevinlee.github.data.GitHubError

/**
  * @author Kevin Lee
  * @since 2019-01-06
  */
object SbtTask {
  // $COVERAGE-OFF$

  type Result[A] = EitherT[SbtTaskHistoryWriter, SbtTaskError, A]

  def fromNonSbtTask[A](
     a: Either[SbtTaskError, A])(
     history: A => List[SbtTaskResult]
   ): Result[A] = EitherT[SbtTaskHistoryWriter, SbtTaskError, A](
      Writer(a.fold(_ => List.empty, aa => history(aa)), a)
    )

  def fromGitTask[A](
    taskResult: => Git.CmdResult[A]
  ): Result[A] =
    EitherT[SbtTaskHistoryWriter, SbtTaskError, A](
      taskResult.leftMap(SbtTaskError.gitCommandTaskError)
        .run
        .mapWritten(_.map(SbtTaskResult.gitCommandTaskResult))
    )

  def toLeftWhen[A](condition: => Boolean, whenFalse: => A): EitherT[SbtTaskHistoryWriter, A, Unit] = EitherT[SbtTaskHistoryWriter, A, Unit] {
    val aOrB = if (condition) whenFalse.left else ().right
    Writer(
      List.empty[SbtTaskResult]
    , aOrB
    )
  }


  def eitherTWithWriter[W: Monoid, A, B](
    r: Either[A, B])(
    fw: B => W
  ): EitherT[Writer[W, ?], A, B] = EitherT[Writer[W, ?], A, B] {
    val w = r match {
      case Left(a) =>
        implicitly[Monoid[W]].zero
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
        val message = if (history.isEmpty) "no task" else "the following tasks"
        println(
          s"""Failure]
             |>> sbt task failed after finishing $message
             |${SbtTaskResult.render(SbtTaskResult.sbtTaskResults(history))}
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
        val message = if (history.isEmpty) "no task" else "the following tasks"
        println(
          s"""Failure]
             |>> sbt task failed after finishing $message
             |${SbtTaskResult.render(SbtTaskResult.taskResult(history))}
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
