package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait IntEqualInstance {
  implicit val intEqual: Equal[Int] = new Equal[Int] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Int, y: Int): Boolean = x == y
  }
}
