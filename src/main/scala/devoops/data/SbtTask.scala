package devoops.data

import SbtTaskResult.{SbtTaskHistory, SbtTaskHistoryWriter}
import cats._
import cats.data._
import cats.implicits._
import effectie.cats.Effectful._
import effectie.cats._
import kevinlee.git.Git
import kevinlee.github.GitHubTask

/** @author Kevin Lee
  * @since 2019-01-06
  */
trait SbtTask[F[_]] {
  // $COVERAGE-OFF$

  import SbtTask._

  def fromNonSbtTask[A](fa: F[Either[SbtTaskError, A]])(
    history: A => List[SbtTaskResult]
  ): Result[F, A]

  def fromGitTask[A](
    taskResult: Git.CmdResult[F, A]
  ): Result[F, A]

  def toLeftWhen[A](
    condition: => Boolean,
    whenFalse: => A,
  ): EitherT[SbtTaskHistoryWriter[F, *], A, Unit]

  def eitherTWithWriter0[W: Monoid, A, B](r: Either[A, B])(
    fw: B => W
  ): EitherT[Writer[W, *], A, B]

  def eitherTWithWriter[W: Monoid, A, B](r: F[Either[A, B]])(
    fw: B => W
  ): EitherT[WriterT[F, W, *], A, B]

  def handleSbtTask(
    sbtTaskResult: F[(SbtTaskHistory, Either[SbtTaskError, Unit])]
  ): F[Unit]

  def handleGitHubTask[A](
    gitHubTaskResult: GitHubTask.GitHubTaskResult[F, A]
  ): Result[F, A]

//    gitHubTaskResult.run.run match {
//      case (history, Left(error)) =>
//        val gitHubTaskError = SbtTaskError.gitHubTaskError(error)
//        val message = if (history.isEmpty) "no task" else "the following tasks"
//        println(
//          s"""Failure]
//             |>> sbt task failed after finishing $message
//             |${SbtTaskResult.render(SbtTaskResult.taskResult(history))}
//             |${SbtTaskError.render(gitHubTaskError)}
//             |""".stripMargin
//        )
//        SbtTaskError.error(gitHubTaskError)
//      case (history, Right(())) =>
//        SbtTaskResult.consolePrintln(
//          SbtTaskResult.taskResult(history)
//        )
//    }

}

object SbtTask {
  // $COVERAGE-OFF$
  type Result[F[_], A] = EitherT[SbtTaskHistoryWriter[F, *], SbtTaskError, A]

  def apply[F[_]: SbtTask]: SbtTask[F] = implicitly[SbtTask[F]]

  implicit def sbtTaskF[F[_]: EffectConstructor: CanCatch: Monad]: SbtTask[F] = new SbtTaskF[F]

  final class SbtTaskF[F[_]: EffectConstructor: CanCatch: Monad] extends SbtTask[F] {

    def fromNonSbtTask[A](fa: F[Either[SbtTaskError, A]])(
      history: A => List[SbtTaskResult]
    ): Result[F, A] = EitherT {
      WriterT(
        fa.map(orA => (orA.fold(_ => List.empty, aa => history(aa)), orA))
      )
    }

    def fromGitTask[A](
      taskResult: Git.CmdResult[F, A]
    ): Result[F, A] =
      EitherT(
        taskResult
          .leftMap(SbtTaskError.gitCommandTaskError)
          .value
          .mapWritten(_.map(SbtTaskResult.gitCommandTaskResult))
      )

    def toLeftWhen[A](
      condition: => Boolean,
      whenFalse: => A,
    ): EitherT[SbtTaskHistoryWriter[F, *], A, Unit] =
      EitherT[SbtTaskHistoryWriter[F, *], A, Unit] {
        val aOrB =
          if (condition)
            whenFalse.asLeft
          else
            ().asRight
        WriterT(
          pureOf(
            (List.empty[SbtTaskResult], aOrB)
          )
        )
      }

    def eitherTWithWriter0[W: Monoid, A, B](r: Either[A, B])(
      fw: B => W
    ): EitherT[Writer[W, ?], A, B] =
      EitherT[Writer[W, ?], A, B] {
        val w =
          r match {
            case Left(a) =>
              Monoid[W].empty

            case Right(b) =>
              fw(b)
          }
        Writer(w, r)
      }

    def eitherTWithWriter[W: Monoid, A, B](r: F[Either[A, B]])(
      fw: B => W
    ): EitherT[WriterT[F, W, *], A, B] =
      EitherT {
        val wf: F[(W, Either[A, B])] = r.map { eth =>
          val w =
            eth match {
              case Left(a) =>
                Monoid[W].empty

              case Right(b) =>
                fw(b)
            }
          (w, eth)
        }
        WriterT(wf)
      }

    def handleSbtTask(
      sbtTaskResult: F[(SbtTaskHistory, Either[SbtTaskError, Unit])]
    ): F[Unit] =
      sbtTaskResult.flatMap {
        case (history, Left(error)) =>
          val message =
            if (history.isEmpty)
              "no task"
            else
              "the following tasks"
          effectOf(
            println(
              s"""Failure]
                 |>> sbt task failed after finishing $message
                 |${SbtTaskResult.render(SbtTaskResult.sbtTaskResults(history))}
                 |${SbtTaskError.render(error)}
                 |""".stripMargin
            )
          ) >> effectOf(SbtTaskError.error[Unit](error))

        case (history, Right(())) =>
          effectOf(
            SbtTaskResult.consolePrintln(
              SbtTaskResult.sbtTaskResults(history)
            )
          )
      }

    def handleGitHubTask[A](
      gitHubTaskResult: GitHubTask.GitHubTaskResult[F, A]
    ): Result[F, A] =
      EitherT[SbtTaskHistoryWriter[F, *], SbtTaskError, A](
        gitHubTaskResult
          .leftMap(SbtTaskError.gitHubTaskError)
          .value
          .mapWritten(_.map(SbtTaskResult.gitHubTaskResult))
      )

    //    gitHubTaskResult.run.run match {
    //      case (history, Left(error)) =>
    //        val gitHubTaskError = SbtTaskError.gitHubTaskError(error)
    //        val message = if (history.isEmpty) "no task" else "the following tasks"
    //        println(
    //          s"""Failure]
    //             |>> sbt task failed after finishing $message
    //             |${SbtTaskResult.render(SbtTaskResult.taskResult(history))}
    //             |${SbtTaskError.render(gitHubTaskError)}
    //             |""".stripMargin
    //        )
    //        SbtTaskError.error(gitHubTaskError)
    //      case (history, Right(())) =>
    //        SbtTaskResult.consolePrintln(
    //          SbtTaskResult.taskResult(history)
    //        )
    //    }
  }
}
