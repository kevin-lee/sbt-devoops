package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait LongEqualInstance {
  implicit val longEqual: Equal[Long] = new Equal[Long] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Long, y: Long): Boolean = x == y
  }
}
