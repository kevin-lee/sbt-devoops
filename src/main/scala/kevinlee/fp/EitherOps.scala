package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-30
  */
object EitherOps {
  implicit class LeftOps[A, B](val l: Left[A, B]) extends AnyVal {
    def castR[C]: Either[A, C] = l.asInstanceOf[Either[A, C]]
  }
  implicit class RightOps[A, B](val r: Right[A, B]) extends AnyVal {
    def castL[C]: Either[C, B] = r.asInstanceOf[Either[C, B]]
  }
}
