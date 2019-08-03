package kevinlee.fp

import JustSyntax.{leftOps, rightOps}
import kevinlee.fp.compat.EitherCompat

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
final case class EitherT[F[_], A, B](run: F[Either[A, B]]) {
  def map[C](f: B => C)(implicit F: Functor[F]): EitherT[F, A, C] =
    EitherT(F.map(run)(EitherCompat.map(_, f)))

  def flatMap[C](f: B => EitherT[F, A, C])(implicit M: Monad[F]): EitherT[F, A, C] =
    EitherT(
      M.flatMap(run) {
        case Right(b) =>
          f(b).run
        case Left(a) =>
          M.pure(Left(a).castR[C])
      }
    )

  def leftMap[C](f: A => C)(implicit F: Functor[F]): EitherT[F, C, B] =
    EitherT(F.map(run)(_.left.map(f)))

  def leftFlatMap[C](f: A => EitherT[F, C, B])(implicit M: Monad[F]): EitherT[F, C, B] =
    EitherT(
      M.flatMap(run) {
        case Left(a) =>
          f(a).run
        case Right(b) =>
          M.pure(Right(b).castL[C])
      }
    )

  def isLeft(implicit F: Functor[F]): F[Boolean] =
    F.map(run)(_.isLeft)

  def isRight(implicit F: Functor[F]): F[Boolean] =
    F.map(run)(_.isRight)

}

object EitherT extends EitherTMonadInstance

sealed abstract class EitherTMonadInstance {
  implicit def eitherTMonad[F[_], A](implicit F0: Monad[F]): Monad[EitherT[F, A, ?]] = new Monad[EitherT[F, A, ?]] {

    implicit val F: Monad[F] = F0

    def flatMap[B, C](fa: EitherT[F, A, B])(f: B => EitherT[F, A, C]): EitherT[F, A, C] =
      fa.flatMap(f)(F)

    def pure[B](b: => B): EitherT[F, A, B] = EitherT(F.pure(Right(b)))
  }
}