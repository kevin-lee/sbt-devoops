package kevinlee.test

import io.estatico.newtype.macros.newtype

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

  @newtype final case class Names(names: List[String])
  @newtype final case class Content(content: String)

}
