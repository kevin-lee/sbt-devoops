package kevinlee.fp

import kevinlee.fp.compat.EitherCompat

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Monad[M[_]] extends Applicative[M] {

  override def map[A, B](ma: M[A])(f: A => B): M[B] =
    flatMap(ma)(a => pure(f(a)))

  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]

  override def ap[A, B](ma: => M[A])(f: => M[A => B]): M[B] =
    flatMap(ma) { a =>
      map(f)(fab => fab(a))
    }

  trait MonadLaw extends ApplicativeLaw {
    /*
     * return a >>= f === f a
     */
    def leftIdentity[A, B](a: A, f: A => M[B])(implicit MB: Equal[M[B]]): Boolean =
      MB.equal(flatMap(pure(a))(f), f(a))

    /*
     * m >>= return === m
     */
    def rightIdentity[A](a: M[A])(implicit MA: Equal[M[A]]): Boolean =
      MA.equal(flatMap(a)(pure(_: A)), a)

    /*
     * (m >>= f) >>= g === m >>= (\x -> f x >>= g)
     */
    def associativity[A, B, C](a: M[A], f: A => M[B], g: B => M[C])(implicit MC: Equal[M[C]]): Boolean =
      MC.equal(flatMap(flatMap(a)(f))(g), flatMap(a)(x => flatMap(f(x))(g)))
  }

  def monadLaw: MonadLaw = new MonadLaw {}
}

object Monad extends MonadInstances

private[fp] trait MonadInstances
  extends IdInstance
    with OptionMonadInstance
    with EitherMonadInstance
    with ListMonadInstance
    with VectorMonadInstance
    with FutureMonadInstance

private[fp] trait IdInstance {

  implicit val idInstance: Functor[Id] with Applicative[Id] with Monad[Id] =
    new Functor[Id] with Applicative[Id] with Monad[Id] {
      override def pure[A](a: => A): Id[A] = a

      override def flatMap[A, B](ma: Id[A])(f: A => Id[B]): Id[B] = f(ma)
    }
}

private[fp] trait OptionMonad extends Monad[Option] with OptionApplicative {

  override def flatMap[A, B](ma: Option[A])(f: A => Option[B]): Option[B] =
    ma.flatMap(f)

  override def pure[A](a: => A): Option[A] = Option(a)
}

private[fp] trait EitherMonad[A] extends Monad[Either[A, *]] with EitherApplicative[A] {

  override def flatMap[B, C](ma: Either[A, B])(f: B => Either[A, C]): Either[A, C] =
    EitherCompat.flatMap(ma)(f)

  override def pure[B](b: => B): Either[A, B] = Right(b)
}

private[fp] trait ListMonad extends Monad[List] with ListApplicative {
  override def flatMap[A, B](ma: List[A])(f: A => List[B]): List[B] =
    ma.flatMap(f)

  override def pure[A](a: => A): List[A] = List(a)
}

private[fp] trait VectorMonad extends Monad[Vector] with VectorApplicative {

  override def flatMap[A, B](ma: Vector[A])(f: A => Vector[B]): Vector[B] =
    ma.flatMap(f)

  override def pure[A](a: => A): Vector[A] = Vector(a)
}

import scala.concurrent.Future
private[fp] trait FutureMonad extends Monad[Future] with FutureApplicative {
  import scala.concurrent.ExecutionContext

  override implicit def executor: ExecutionContext

  override def flatMap[A, B](ma: Future[A])(f: A => Future[B]): Future[B] =
    ma.flatMap(f)

  override def pure[A](a: => A): Future[A] = Future(a)
}

private[fp] trait OptionMonadInstance extends OptionApplicativeInstance {
  implicit val optionMonad: Monad[Option] = new OptionMonad {}
}

private[fp] trait EitherMonadInstance extends EitherApplicativeInstance {
  implicit def eitherMonad[A]: Monad[Either[A, *]] = new EitherMonad[A] {}
}

private[fp] trait ListMonadInstance extends ListApplicativeInstance {
  implicit val listMonad: Monad[List] = new ListMonad {}
}

private[fp] trait VectorMonadInstance extends VectorApplicativeInstance {
  implicit val vectorMonad: Monad[Vector] = new VectorMonad {}
}

private[fp] trait FutureMonadInstance extends FutureApplicativeInstance {
  import scala.concurrent.ExecutionContext

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  implicit def futureMonad(implicit executor0: ExecutionContext): Monad[Future] =
    new FutureMonad {
      override implicit def executor: ExecutionContext = executor0
    }
}
