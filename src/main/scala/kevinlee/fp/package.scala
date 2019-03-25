package kevinlee

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
package object fp {
  type Id[X] = X

  implicit val idInstance: Monad[Id] = new Monad[Id] {

    def flatMap[A, B](a: A)(f: A => B): B = f(a)

    def pure[A](a: A): A = a
  }
}
