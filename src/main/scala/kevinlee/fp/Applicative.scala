package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: => A): F[A]

  override def map[A, B](fa: F[A])(f: A => B): F[B] = ap(fa)(pure(f))

  def ap[A, B](fa: => F[A])(fab: => F[A => B]): F[B]

  trait ApplicativeLaw extends FunctorLaw {
    /* Identity
     * pure id <*> v = v
     */
    def identityAp[A](fa: => F[A])(implicit FA: Equal[F[A]]): Boolean =
      FA.equal(
        ap(fa)(pure(scala.Predef.identity))
      , fa
      )

    /* Homomorphism
     * pure f <*> pure x = pure (f x)
     */
    def homomorphism[A, B](f: A => B, a: => A)(implicit FB: Equal[F[B]]): Boolean =
      FB.equal(
        ap(pure(a))(pure(f))
      , pure(f(a))
      )

    /* Interchange
     * u <*> pure y = pure ($ y) <*> u
     */
    def interchange[A, B](a: => A, f: F[A => B])(implicit FB: Equal[F[B]]): Boolean =
      FB.equal(
        ap(pure(a))(f)
      , ap(f)(pure(g => g(a)))
      )

    /* Composition
     * pure (.) <*> u <*> v <*> w = u <*> (v <*> w)
     */
    def compositionAp[A, B, C](fa: F[A], f: F[B => C], g: F[A => B])(implicit FC: Equal[F[C]]): Boolean =
      FC.equal(
        ap(fa)(ap(g)(ap(f)(pure(bc => ab => bc compose ab))))
      , ap(ap(fa)(g))(f)
      )
  }

  def applicativeLaw: ApplicativeLaw = new ApplicativeLaw {}
}

object Applicative {

  implicit def applicativeEither[L]: Applicative[Either[L, ?]] =
    new Applicative[Either[L, ?]] {
      def pure[A](a: => A): Either[L, A] = Right(a)

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