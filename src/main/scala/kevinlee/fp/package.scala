package kevinlee

/**
  * @author Kevin Lee
  * @since 2019-03-24
  */
package object fp extends IdInstance {

  type Writer[L, V] = WriterT[Id, L, V]

  object Writer {
    def apply[W, A](w: W, a: A): WriterT[Id, W, A] = WriterT[Id, W, A]((w, a))
  }
}
