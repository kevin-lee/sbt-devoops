package kevinlee.fp.syntax

import kevinlee.fp.Equal

import scala.language.implicitConversions

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
object EqualSyntax {
  final class EqualOps[A] private[syntax] (val eqLeft: A) extends AnyVal {
    def ===(eqRight: A)(implicit E: Equal[A]): Boolean =
      E.equal(eqLeft, eqRight)
    def !==(eqRight: A)(implicit E: Equal[A]): Boolean =
      !E.equal(eqLeft, eqRight)
  }
}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
trait EqualSyntax {
  import EqualSyntax._

  implicit def ToEqualOps[A: Equal](eqLeft: A): EqualOps[A] =
    new EqualOps(eqLeft)
}