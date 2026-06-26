package kevinlee.test

import refined4s.*

/** @author Kevin Lee
  * @since 2019-03-02
  */
trait dataBase {
  type Names = dataBase.Names
  val Names = dataBase.Names

  type Content = dataBase.Content
  val Content = dataBase.Content
}
object dataBase {

  type Names = Names.Type
  object Names extends Newtype[List[String]] {
    extension (a: Names) def names: List[String] = a.value
  }

  type Content = Content.Type
  object Content extends Newtype[String] {
    extension (a: Content) def content: String = a.value
  }

}
