package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait SemiGroup[A] {
  def append(a1: A, a2: => A): A
}

object SemiGroup {

  trait ListSemiGroup[A] extends SemiGroup[List[A]] {
    override def append(a1: List[A], a2: => List[A]): List[A] = a1 ++ a2
  }
  implicit def listSemiGroup[A]: SemiGroup[List[A]] = new ListSemiGroup[A] {}

  trait VectorSemiGroup[A] extends SemiGroup[Vector[A]] {
    override def append(a1: Vector[A], a2: => Vector[A]): Vector[A] = a1 ++ a2
  }
  implicit def vectorSemiGroup[A]: SemiGroup[Vector[A]] = new VectorSemiGroup[A] {}

  trait StringSemiGroup extends SemiGroup[String] {
    override def append(a1: String, a2: => String): String = a1 + a2
  }
  implicit val stringSemiGroup: SemiGroup[String] = new StringSemiGroup {}

  trait ByteSemiGroup extends SemiGroup[Byte]{
    override def append(a1: Byte, a2: => Byte): Byte = (a1 + a2).toByte
  }
  implicit val byteSemiGroup: SemiGroup[Byte] = new ByteSemiGroup {}

  trait ShortSemiGroup extends SemiGroup[Short] {
    override def append(a1: Short, a2: => Short): Short = (a1 + a2).toShort
  }
  implicit val shortSemiGroup: SemiGroup[Short] = new ShortSemiGroup {}

  trait CharSemiGroup extends SemiGroup[Char] {
    override def append(a1: Char, a2: => Char): Char = (a1 + a2).toChar
  }
  implicit val charSemiGroup: SemiGroup[Char] = new CharSemiGroup {}

  trait IntSemiGroup extends SemiGroup[Int] {
    override def append(a1: Int, a2: => Int): Int = a1 + a2
  }
  implicit val intSemiGroup: SemiGroup[Int] = new IntSemiGroup {}

  trait LongSemiGroup extends SemiGroup[Long] {
    override def append(a1: Long, a2: => Long): Long = a1 + a2
  }
  implicit val longSemiGroup: SemiGroup[Long] = new LongSemiGroup {}

  trait BigIntSemiGroup extends SemiGroup[BigInt] {
    override def append(a1: BigInt, a2: => BigInt): BigInt = a1 + a2
  }
  implicit val bigIntSemiGroup: SemiGroup[BigInt] = new BigIntSemiGroup {}

  trait BigDecimalSemiGroup extends SemiGroup[BigDecimal] {
    override def append(a1: BigDecimal, a2: => BigDecimal): BigDecimal = a1 + a2
  }
  implicit val bigDecimalSemiGroup: SemiGroup[BigDecimal] = new BigDecimalSemiGroup {}
}
