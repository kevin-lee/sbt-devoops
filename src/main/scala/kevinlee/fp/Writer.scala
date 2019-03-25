package kevinlee.fp

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
object Writer {
  type Writer[L, V] = WriterT[Id, L, V]

  def apply[W, A](w: W, a: A): WriterT[Id, W, A] = WriterT[Id, W, A]((w, a))
}