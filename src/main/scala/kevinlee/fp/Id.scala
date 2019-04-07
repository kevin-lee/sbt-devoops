package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-04-03
  */
trait IdInstance {

  type Id[X] = X

  implicit val idMonad: Monad[Id] = new Monad[Id] {

    def flatMap[A, B](a: A)(f: A => B): B = f(a)

    def pure[A](a: => A): A = a
  }

}

object Id extends IdInstance