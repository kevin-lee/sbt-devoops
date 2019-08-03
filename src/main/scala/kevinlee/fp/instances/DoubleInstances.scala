package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait DoubleEqualInstance {
  implicit val doubleEqual: Equal[Double] = new Equal[Double] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Double, y: Double): Boolean = x == y
  }
}
