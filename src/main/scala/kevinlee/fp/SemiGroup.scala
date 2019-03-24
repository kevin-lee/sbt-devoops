package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-16
  */
trait SemiGroup[A] {
  def append(a1: A, a2: A): A
}
