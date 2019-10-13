package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
final case class WriterT[F[_], W, A](run: F[(W, A)]) {
  import WriterT.writerT

  def map[B](f: A => B)(implicit F: Functor[F]): WriterT[F, W, B] =
    writerT(F.map(run)(wa => (wa._1, f(wa._2))))

  def ap[B](fa: => WriterT[F, W, A => B])(implicit F: Applicative[F], S: SemiGroup[W]): WriterT[F, W, B] =
    WriterT {
      F.ap(run)(F.map(fa.run) { case (w2, a2) =>
        wa1 => (S.append(wa1._1, w2), a2(wa1._2))
      })
    }

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

sealed trait WriterTMonadInstances extends WriterMonadInstance

object WriterT extends WriterTMonadInstances {
  def writerT[F[_], W, A](f: F[(W, A)]): WriterT[F, W, A] = WriterT(f)
}

private trait WriterTFunctor[F[_], W] extends Functor[WriterT[F, W, *]] {
  implicit def F: Functor[F]

  override def map[A, B](fa: WriterT[F, W, A])(f: A => B): WriterT[F, W, B] = fa.map(f)(F)
}

private trait WriterTApplicative[F[_], W] extends Applicative[WriterT[F, W, *]] with WriterTFunctor[F, W] {
  override implicit def F: Applicative[F]
  implicit def W: Monoid[W]

  override def pure[A](a: => A): WriterT[F, W, A] = WriterT.writerT(F.pure((W.zero, a)))

  override def ap[A, B](fa: => WriterT[F, W, A])(fab: => WriterT[F, W, A => B]): WriterT[F, W, B] =
    fa.ap(fab)(F, W)
}

private trait WriterTMonad[F[_], W] extends Monad[WriterT[F, W, *]] with WriterTApplicative[F, W] {
  override implicit def F: Monad[F]

  override def flatMap[A, B](ma: WriterT[F, W, A])(f: A => WriterT[F, W, B]): WriterT[F, W, B] =
    ma.flatMap(f)(F, W)
}

sealed abstract class WriterTFunctorInstance {

  implicit def writerTMonad[F[_], W](implicit F0: Functor[F]): Functor[WriterT[F, W, *]] =
    new WriterTFunctor[F, W] {
      implicit val F: Functor[F] = F0
    }
}
sealed abstract class WriterTApplicativeInstance extends WriterTFunctorInstance {

  implicit def writerTMonad[F[_], W](implicit F0: Applicative[F], S0: Monoid[W]): Applicative[WriterT[F, W, *]] =
    new WriterTApplicative[F, W] {
      override implicit val F: Applicative[F] = F0
      implicit val W: Monoid[W] = S0
    }
}

sealed abstract class WriterTMonadInstance extends WriterTApplicativeInstance {

  implicit def writerTMonad[F[_], W](implicit F0: Monad[F], S0: Monoid[W]): Monad[WriterT[F, W, *]] =
    new WriterTMonad[F, W] {
      override implicit val F: Monad[F] = F0
      override implicit val W: Monoid[W] = S0
    }

  implicit def writerTEqual[F[_], W, A](implicit EQ: Equal[F[(W, A)]]): Equal[WriterT[F, W, A]] =
    new Equal[WriterT[F, W, A]] {
      override def equal(x: WriterT[F, W, A], y: WriterT[F, W, A]): Boolean =
        EQ.equal(x.run, y.run)
    }

}

sealed abstract class WriterMonadInstance extends WriterTMonadInstance {

  implicit def writerMonad[W](implicit S0: Monoid[W]): Monad[Writer[W, *]] =
    new WriterTMonad[Id, W] {
      override implicit val F: Functor[Id] with Applicative[Id] with Monad[Id] = idInstance
      override implicit val W: Monoid[W] = S0
    }

  implicit def writerEqual[W, A](implicit EQ: Equal[(W, A)]): Equal[Writer[W, A]] =
    new Equal[Writer[W, A]] {
      override def equal(x: Writer[W, A], y: Writer[W, A]): Boolean =
        EQ.equal(x.run, y.run)
    }
}
