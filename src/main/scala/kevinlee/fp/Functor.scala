package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]

  def lift[A, B](f: A => B): F[A] => F[B] = fa => map(fa)(f)
}

object Functor {

  implicit val listFunctor: Functor[List] = new Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
  }

  implicit val vectorFunctor: Functor[Vector] = new Functor[Vector] {
    def map[A, B](fa: Vector[A])(f: A => B): Vector[B] = fa.map(f)
  }

}