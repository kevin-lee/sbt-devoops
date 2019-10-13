package kevinlee.fp

import kevinlee.fp.SemiGroup.{BigDecimalSemiGroup, BigIntSemiGroup, ByteSemiGroup, CharSemiGroup, IntSemiGroup, ListSemiGroup, LongSemiGroup, ShortSemiGroup, StringSemiGroup, VectorSemiGroup}

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait Monoid[A] extends SemiGroup[A] {
  def zero: A

  @SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
  trait MonoidLaw {
    /*
     * (x <> y) <> z = x <> (y <> z) -- associativity
     */
    def associativity(ma: Monoid[A], x: A, y: A, z: A)(implicit EA: Equal[A]): Boolean =
      EA.equal(ma.append(ma.append(x, y), z), ma.append(x, ma.append(y, z)))

    /*
     * mempty <> x = x -- left identity
     */
    def leftIdentity(ma: Monoid[A], x: A)(implicit EA: Equal[A]): Boolean =
      EA.equal(ma.append(ma.zero, x), x)

    /*
     * x <> mempty = x -- right identity
     */
    def rightIdentity(ma: Monoid[A], x: A)(implicit EA: Equal[A]): Boolean =
      EA.equal(ma.append(x, ma.zero), x)
  }

  def monoidLaw: MonoidLaw = new MonoidLaw {}
}

object Monoid {

  implicit def listMonoid[A]: Monoid[List[A]] = new Monoid[List[A]] with ListSemiGroup[A] {
    override def zero: List[A] = Nil
  }

  implicit def vectorMonoid[A]: Monoid[Vector[A]] = new Monoid[Vector[A]] with VectorSemiGroup[A] {
    override def zero: Vector[A] = Vector.empty
  }

  implicit val stringMonoid: Monoid[String] = new Monoid[String] with StringSemiGroup {
    override def zero: String = ""
  }

  implicit val byteMonoid: Monoid[Byte] = new Monoid[Byte] with ByteSemiGroup {
    override def zero: Byte = 0.toByte
  }

  implicit val shortMonoid: Monoid[Short] = new Monoid[Short] with ShortSemiGroup {
    override def zero: Short = 0.toShort
  }

  implicit val charMonoid: Monoid[Char] = new Monoid[Char] with CharSemiGroup {
    override def zero: Char = Char.MinValue
  }

  implicit val intMonoid: Monoid[Int] = new Monoid[Int] with IntSemiGroup {
    override def zero: Int = 0
  }

  implicit val longMonoid: Monoid[Long] = new Monoid[Long] with LongSemiGroup {
    override def zero: Long = 0L
  }

  implicit val bigIntMonoid: Monoid[BigInt] = new Monoid[BigInt] with BigIntSemiGroup {
    override def zero: BigInt = BigInt(0)
  }

  implicit val bigDecimalMonoid: Monoid[BigDecimal] = new Monoid[BigDecimal] with BigDecimalSemiGroup {
    override def zero: BigDecimal = BigDecimal(0)
  }

}