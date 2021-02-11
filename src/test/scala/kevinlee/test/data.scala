package kevinlee.test

import io.estatico.newtype.macros.newtype

/** @author Kevin Lee
  * @since 2019-03-02
  */
@SuppressWarnings(Array(
  "org.wartremover.warts.ExplicitImplicitTypes",
  "org.wartremover.warts.ImplicitConversion",
  "org.wartremover.warts.ImplicitParameter",
  "org.wartremover.warts.PublicInference",
))
object data {

  @newtype case class Names(names: List[String])
  @newtype case class Content(content: String)
  final case class NamesAndContent(names: Names, content: Content)

}
