package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait FloatEqualInstance {
  implicit val floatEqual: Equal[Float] = new Equal[Float] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Float, y: Float): Boolean = x == y
  }
}
