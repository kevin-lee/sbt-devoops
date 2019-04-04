package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: => A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B]

  def ap[A, B](fa: => F[A])(fab: => F[A => B]): F[B]
}

object Applicative {

  implicit def applicativeEither[L]: Applicative[({ type AA[A] = Either[L, A] })#AA] =
    new Applicative[({ type AA[A] = Either[L, A] })#AA] {
      def pure[A](a: => A): Either[L, A] = Right(a)

      def map[A, B](fa: Either[L, A])(f: A => B): Either[L, B] =
        fa match {
          case Right(a) =>
            Right(f(a))
          case Left(l) =>
            Left(l)
        }

      def ap[A, B](fa: => Either[L, A])(fb: => Either[L, A => B]): Either[L, B] =
        (fa, fb) match {
          case (Right(a), Right(bf)) =>
            Right(bf(a))
          case (Left(l), _) =>
            Left(l)
          case (_, Left(l)) =>
            Left(l)
        }
    }
}