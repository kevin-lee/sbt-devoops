package kevinlee.fp.instances

import kevinlee.fp.Equal

/**
 * @author Kevin Lee
 * @since 2019-09-20
 */
trait ByteEqualInstance {
  implicit val byteEqual: Equal[Byte] = new Equal[Byte] {
    @SuppressWarnings(Array("org.wartremover.warts.Equals"))
    override def equal(x: Byte, y: Byte): Boolean = x == y
  }
}
