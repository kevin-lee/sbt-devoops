package example

import hedgehog._
import hedgehog.runner._

object HelloSpec extends Properties {
  def tests: List[Test] = List(
    example("The Hello object should say hello", testHello)
  )
  def testHello: Result =
    Hello.greeting ==== "hello"

}
