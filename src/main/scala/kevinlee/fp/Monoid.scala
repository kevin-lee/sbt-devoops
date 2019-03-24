package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Monoid[A] extends SemiGroup[A] {
  def zero: A
}

object Monoid {

  implicit def listMonoid[A]: Monoid[List[A]] = new Monoid[List[A]] {
    override def zero: List[A] = Nil

    override def append(a1: List[A], a2: List[A]): List[A] = a1 ++ a2
  }

  implicit def vectorMonoid[A]: Monoid[Vector[A]] = new Monoid[Vector[A]] {
    override def zero: Vector[A] = Vector.empty

    override def append(a1: Vector[A], a2: Vector[A]): Vector[A] = a1 ++ a2
  }

}