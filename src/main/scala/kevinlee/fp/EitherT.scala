package kevinlee.fp

import JustSyntax.{leftOps, rightOps}
import kevinlee.fp.compat.EitherCompat

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
final case class EitherT[F[_], A, B](run: F[Either[A, B]]) {
  def map[C](f: B => C)(implicit F: Functor[F]): EitherT[F, A, C] =
    EitherT(F.map(run)(EitherCompat.map(_)(f)))

  def ap[C](fa: EitherT[F, A, B => C])(implicit F: Applicative[F]): EitherT[F, A, C] =
    EitherT(
      F.ap(run)(F.map(fa.run) {
        case fb @ Right(_) => {
          case Right(b) =>
            EitherCompat.map(fb)(fb => fb(b))
          case l @ Left(_) =>
            l.castR[C]
        }
        case  l @ Left(_) => _ =>
          l.castR[C]
      })
    )

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

object EitherT extends EitherTMonadInstance {
  def pure[F[_]: Applicative, A, B](b: B): EitherT[F, A, B] =
    EitherT(implicitly[Applicative[F]].pure(Right(b)))

  def pureLeft[F[_]: Applicative, A, B](a: A): EitherT[F, A, B] =
    EitherT(implicitly[Applicative[F]].pure(Left(a)))
}

private trait EitherTFunctor[F[_], A] extends Functor[EitherT[F, A, *]] {
  implicit def F: Functor[F]

  override def map[B, C](fa: EitherT[F, A, B])(f: B => C): EitherT[F, A, C] =
    fa.map(f)(F)
}

private trait EitherTApplicative[F[_], A] extends Applicative[EitherT[F, A, *]] with EitherTFunctor[F, A] {
  implicit def F: Applicative[F]

  override def pure[B](b: => B): EitherT[F, A, B] = EitherT(F.pure(Right(b)))

  def pureLef[B](a: => A): EitherT[F, A, B] = EitherT(F.pure(Left(a)))

  override def ap[B, C](fa: => EitherT[F, A, B])(fab: => EitherT[F, A, B => C]): EitherT[F, A, C] =
    fa.ap(fab)(F)
}

private trait EitherTMonad[F[_], A] extends Monad[EitherT[F, A, *]] with EitherTApplicative[F, A] {
  implicit def F: Monad[F]
}

sealed abstract class EitherTFunctorInstance {
  implicit def eitherTFunctor[F[_], A](implicit F0: Functor[F]): Functor[EitherT[F, A, *]] = new EitherTFunctor[F, A] {
    override implicit val F: Functor[F] = F0
  }
}

sealed abstract class EitherTApplicativeInstance extends EitherTFunctorInstance {
  implicit def eitherTFunctor[F[_], A](implicit F0: Applicative[F]): Applicative[EitherT[F, A, *]] =
    new EitherTApplicative[F, A] {
      override implicit val F: Applicative[F] = F0
    }
}

sealed abstract class EitherTMonadInstance extends EitherTApplicativeInstance {

  implicit def eitherTMonad[F[_], A](implicit F0: Monad[F]): Monad[EitherT[F, A, *]] = new EitherTMonad[F, A] {

    override implicit val F: Monad[F] = F0

    override def flatMap[B, C](ma: EitherT[F, A, B])(f: B => EitherT[F, A, C]): EitherT[F, A, C] =
      ma.flatMap(f)(F)

  }

  implicit def eitherTEqual[F[_], A, B](implicit EQ: Equal[F[Either[A, B]]]): Equal[EitherT[F, A, B]] =
    new Equal[EitherT[F, A, B]] {
      override def equal(x: EitherT[F, A, B], y: EitherT[F, A, B]): Boolean =
        EQ.equal(x.run, y.run)
    }

}