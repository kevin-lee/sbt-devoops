package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
final case class EitherT[F[_], A, B](run: F[Either[A, B]]) {
  def map[C](f: B => C)(implicit F: Functor[F]): EitherT[F, A, C] =
    EitherT(F.map(run)(_.right.map(f)))

  def flatMap[C](f: B => EitherT[F, A, C])(implicit M: Monad[F]): EitherT[F, A, C] =
    EitherT(
      M.flatMap(run) {
        case Right(b) =>
          f(b).run
        case Left(a) =>
          M.pure(Left(a).asInstanceOf[Either[A, C]])
      }
    )
}

object EitherT {
  def eitherT[F[_], A, B](either: F[Either[A, B]]): EitherT[F, A, B] = apply(either)

  implicit def eitherTFunctor[F[_], A](implicit F: Functor[F]): Functor[({ type AA[B] = EitherT[F, A, B] })#AA] = new Functor[({ type AA[B] = EitherT[F, A, B] })#AA] {

    override def map[B, C](fa: EitherT[F, A, B])(f: B => C): EitherT[F, A, C] =
      fa.map(f)
  }

  implicit def eitherTMonad[F[_], A](implicit F: Monad[F]): Monad[({ type AA[B] = EitherT[F, A, B] })#AA] = new Monad[({ type AA[B] = EitherT[F, A, B] })#AA] {

    def flatMap[B, C](fa: EitherT[F, A, B])(f: B => EitherT[F, A, C]): EitherT[F, A, C] =
      fa.flatMap(f)

    def pure[B](b: B): EitherT[F, A, B] = EitherT(F.pure(Right(b)))
  }
}