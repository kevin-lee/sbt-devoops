package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait CharEqualInstance {
  implicit val charEqual: Equal[Char] = new Equal[Char] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Char, y: Char): Boolean = x == y
  }
}
