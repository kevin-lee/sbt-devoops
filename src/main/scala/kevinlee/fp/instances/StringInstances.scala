package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
  * @author Kevin Lee
  * @since 2019-07-28
  */
trait StringEqualInstance {
  implicit val stringEqual: Equal[String] = new Equal[String] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: String, y: String): Boolean = x == y
  }
}
