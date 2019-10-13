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

object Applicative extends ApplicativeInstances

private[fp] trait ApplicativeInstances
  extends OptionApplicativeInstance
    with EitherApplicativeInstance
    with ListApplicativeInstance
    with VectorApplicativeInstance
    with FutureApplicativeInstance

private[fp] trait OptionApplicative extends Applicative[Option] with OptionFunctor {

  override def pure[A](a: => A): Option[A] = Some(a)

  override def ap[A, B](fa: => Option[A])(fb: => Option[A => B]): Option[B] =
    (fa, fb) match {
      case (Some(a), Some(bf)) =>
        Some(bf(a))
      case (None, _) =>
        None
      case (_, None) =>
        None
    }
}

private[fp] trait EitherApplicative[A] extends Applicative[Either[A, *]] with EitherFunctor[A] {

  override def pure[B](b: => B): Either[A, B] = Right(b)

  override def ap[B, C](fb: => Either[A, B])(fc: => Either[A, B => C]): Either[A, C] =
    (fb, fc) match {
      case (Right(b), Right(cf)) =>
        Right(cf(b))
      case (Left(a), _) =>
        Left(a)
      case (_, Left(a)) =>
        Left(a)
    }
}

private[fp] trait ListApplicative extends Applicative[List] with ListFunctor {

  override def pure[A](a: => A): List[A] = List(a)

  override def ap[A, B](fa: => List[A])(fab: => List[A => B]): List[B] =
    fab.flatMap(f => fa.map(f))
}

private[fp] trait VectorApplicative extends Applicative[Vector] with VectorFunctor {

  override def pure[A](a: => A): Vector[A] = Vector(a)

  override def ap[A, B](fa: => Vector[A])(fab: => Vector[A => B]): Vector[B] =
    fab.flatMap(f => fa.map(f))
}

import scala.concurrent.Future
private[fp] trait FutureApplicative extends Applicative[Future] with FutureFunctor {
  import scala.concurrent.ExecutionContext

  override implicit def executor: ExecutionContext

  override def pure[A](a: => A): Future[A] = Future(a)

  override def ap[A, B](fa: => Future[A])(fab: => Future[A => B]): Future[B] =
    fab.flatMap(f => fa.map(f))
}

private[fp] trait OptionApplicativeInstance extends OptionFunctorInstance {
  implicit val applicativeOption: Applicative[Option[*]] = new OptionApplicative {}
}

private[fp] trait EitherApplicativeInstance extends EitherFunctorInstance {
  implicit def applicativeEither[A]: Applicative[Either[A, *]] =
    new EitherApplicative[A] {}

}

private[fp] trait ListApplicativeInstance extends ListFunctorInstance {
  implicit val applicativeList: Applicative[List] = new ListApplicative {}
}

private[fp] trait VectorApplicativeInstance extends VectorFunctorInstance {
  implicit val applicativeVector: Applicative[Vector] = new VectorApplicative {}
}

private[fp] trait FutureApplicativeInstance extends FutureFunctorInstance {
  import scala.concurrent.ExecutionContext

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def applicativeFuture(implicit executor0: ExecutionContext): Applicative[Future] =
    new FutureApplicative {
      implicit def executor: ExecutionContext = executor0
    }
}
