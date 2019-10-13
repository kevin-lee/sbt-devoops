package kevinlee.fp

import kevinlee.fp.compat.EitherCompat

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]

  def lift[A, B](f: A => B): F[A] => F[B] = fa => map(fa)(f)

  trait FunctorLaw {
    /* Functors must preserve identity morphisms
     * fmap id = id
     */
    def identity[A](fa: F[A])(implicit FA: Equal[F[A]]): Boolean =
      FA.equal(map(fa)(scala.Predef.identity), fa)

    /* Functors preserve composition of morphisms
     * fmap (f . g)  ==  fmap f . fmap g
     */
    def composition[A, B, C](fa: F[A], f: B => C, g: A => B)(implicit FC: Equal[F[C]]): Boolean =
      FC.equal(map(fa)(f compose g), map(map(fa)(g))(f))
  }

  def functorLaw: FunctorLaw = new FunctorLaw {}
}

object Functor extends FunctorInstances

private[fp] trait FunctorInstances
  extends OptionFunctorInstance
    with EitherFunctorInstance
    with ListFunctorInstance
    with VectorFunctorInstance
    with FutureFunctorInstance

private[fp] trait OptionFunctor extends Functor[Option] {
  override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
}

private[fp] trait EitherFunctor[A] extends Functor[Either[A, *]] {
  override def map[B, C](fa: Either[A, B])(f: B => C): Either[A, C] =
    EitherCompat.map(fa)(f)
}

private[fp] trait ListFunctor extends Functor[List] {
  override def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
}

private[fp] trait VectorFunctor extends Functor[Vector] {
  override def map[A, B](fa: Vector[A])(f: A => B): Vector[B] = fa.map(f)
}

import scala.concurrent.Future
private[fp] trait FutureFunctor extends Functor[Future] {
  import scala.concurrent.ExecutionContext
  implicit def executor: ExecutionContext

  override def map[A, B](fa: Future[A])(f: A => B): Future[B] =
    fa.map(f)(executor)
}

private[fp] trait OptionFunctorInstance {
  implicit val optionFunctor: Functor[Option] = new OptionFunctor {}
}

private[fp] trait EitherFunctorInstance {
  implicit def eitherFunctor[A]: Functor[Either[A, *]] = new EitherFunctor[A] {}
}

private[fp] trait ListFunctorInstance {
  implicit val listFunctor: Functor[List] = new ListFunctor {}
}

private[fp] trait VectorFunctorInstance {
  implicit val vectorFunctor: Functor[Vector] = new VectorFunctor {}
}

private[fp] trait FutureFunctorInstance {
  import scala.concurrent.ExecutionContext

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def futureFunctor(implicit executor0: ExecutionContext): Functor[Future] =
    new FutureFunctor {
      override implicit def executor: ExecutionContext = executor0
    }
}
