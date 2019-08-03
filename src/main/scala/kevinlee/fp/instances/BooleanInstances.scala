package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait BooleanEqualInstance {
  implicit val booleanEqual: Equal[Boolean] = new Equal[Boolean] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Boolean, y: Boolean): Boolean = x == y
  }
}
