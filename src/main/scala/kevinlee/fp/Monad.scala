package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Monad[F[_]] extends Applicative[F] {
  def map[A, B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)(a => pure(f(a)))

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def ap[A, B](fa: F[A])(f: F[A => B]): F[B] =
    flatMap(fa) { a =>
      map(f)(fab => fab(a))
    }

}
