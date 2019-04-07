package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
final case class WriterT[F[_], W, A](run: F[(W, A)]) {
  import WriterT.writerT

  def map[B](f: A => B)(implicit F: Functor[F]): WriterT[F, W, B] =
    writerT(F.map(run)(wa => (wa._1, f(wa._2))))

  def ap[B](fa: => WriterT[F, W, A => B])(implicit M: Monad[F], S: SemiGroup[W]): WriterT[F, W, B] =
    writerT(
      M.flatMap(run){ wa =>
        M.map(fa.run) { wfa =>
          (S.append(wa._1, wfa._1), wfa._2(wa._2))
        }
      }
    )

  def flatMap[B](f: A => WriterT[F, W, B])(implicit M: Monad[F], S: SemiGroup[W]): WriterT[F, W, B] =
    writerT(M.flatMap(run) { wa =>
      val fb = f(wa._2).run
      M.map(fb)(wb => (S.append(wa._1, wb._1), wb._2))
    })

  def mapWritten[X](w: W => X)(implicit F: Functor[F]): WriterT[F, X, A] = WriterT[F, X, A] {
    F.map(run)(wa => (w(wa._1), wa._2))
  }

  def written(implicit F: Functor[F]): F[W] =
    F.map(run)(_._1)

  def value(implicit F: Functor[F]): F[A] =
    F.map(run)(_._2)
}

object WriterT extends WriterTMonadInstance {
  def writerT[F[_], W, A](f: F[(W, A)]): WriterT[F, W, A] = WriterT(f)
}

sealed abstract class WriterTMonadInstance extends WriterInstance {

  implicit def writerTMonad[F[_], W](implicit F0: Monad[F], S0: Monoid[W]): Monad[({ type AA[C] = WriterT[F, W, C] })#AA] = new Monad[({ type AA[C] = WriterT[F, W, C] })#AA] {
    implicit val F: Monad[F] = F0
    implicit val S: Monoid[W] = S0

    def flatMap[A, B](fa: WriterT[F, W, A])(f: A => WriterT[F, W, B]): WriterT[F, W, B] =
      fa.flatMap(f)(F, S)

    def pure[A](a: => A): WriterT[F, W, A] = WriterT(F.pure((S.zero, a)))
  }
}

sealed abstract class WriterInstance {

  implicit def writerMonad[W](implicit S0: Monoid[W]): Monad[({ type AA[A] = WriterT[Id, W, A] })#AA] = new Monad[({ type AA[A] = WriterT[Id, W, A] })#AA] {
    implicit val F: Monad[Id] = idMonad
    implicit val S: Monoid[W] = S0

    def flatMap[A, B](fa: WriterT[Id, W, A])(f: A => WriterT[Id, W, B]): WriterT[Id, W, B] =
      fa.flatMap(f)(F, S)

    def pure[A](a: => A): WriterT[Id, W, A] = WriterT(F.pure((S.zero, a)))
  }
}

