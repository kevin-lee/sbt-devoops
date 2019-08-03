package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait BigIntEqualInstance {
  implicit val bigIntEqual: Equal[BigInt] = new Equal[BigInt] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: BigInt, y: BigInt): Boolean = x == y
  }
}
