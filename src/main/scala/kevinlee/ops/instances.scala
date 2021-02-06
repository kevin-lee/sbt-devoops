package kevinlee.ops

import cats.Show

import java.io.{PrintWriter, StringWriter}

/**
 * @author Kevin Lee
 * @since 2021-02-06
 */
object instances extends ShowInstances

trait ShowInstances {
  implicit val showThrowable: Show[Throwable] = { throwable =>
    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    throwable.printStackTrace(printWriter)
    throwable.toString
  }
}
