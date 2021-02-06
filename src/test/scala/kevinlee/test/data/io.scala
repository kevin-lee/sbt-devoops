package kevinlee.test.data

/** @author Kevin Lee
  * @since 2019-03-02
  */
final case class Names(names: List[String]) extends AnyVal
final case class Content(content: String)   extends AnyVal
final case class NamesAndContent(names: Names, content: Content)
