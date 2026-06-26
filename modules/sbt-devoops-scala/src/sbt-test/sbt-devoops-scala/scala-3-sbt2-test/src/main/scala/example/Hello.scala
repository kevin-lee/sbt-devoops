package example

object Hello extends Greeting {
  @main def run(): Unit = println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello"
}
