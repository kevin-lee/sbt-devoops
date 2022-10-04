package devoops.types

import devoops.data.DevOopsLogLevel
import kevinlee.github.data.GitHubError
import cats.syntax.all.*

import kevinlee.ops.instances.*

import scala.util.control.NoStackTrace

/** @author Kevin Lee
  * @since 2022-05-28
  */
@SuppressWarnings(Array("org.wartremover.warts.Null"))
sealed abstract class StarterError(val message: String, val cause: Option[Throwable])
    extends RuntimeException(message, cause.orNull) {
  override def getMessage: String = message
}

object StarterError {
  sealed abstract class SysError private[StarterError] (
    override val message: String,
    override val cause: Option[Throwable]
  ) extends StarterError(message, cause)

  sealed abstract class AppError private[StarterError] (override val message: String)
      extends StarterError(message, none)
      with NoStackTrace

  final case class Unexpected(when: String, error: Throwable)
      extends SysError(s"When $when, ${error.getMessage}", error.some)

  def unexpected(when: String, error: Throwable): StarterError = Unexpected(when, error)

  final case class ResourceReadWrite(when: String, override val message: String)
      extends AppError(s"When $when, message: $message")

  final case class GitHub(when: String, gitHubError: GitHubError) extends AppError("When accessing GitHub API")

  def resourceReadWrite(when: String, message: String): StarterError =
    ResourceReadWrite(when, message)

  def gitHub(when: String, gitHubError: GitHubError): StarterError = GitHub(when, gitHubError)

  implicit class StarterErrorOps(private val starterError: StarterError) extends AnyVal {
    def render(implicit L: DevOopsLogLevel): String = starterError match {
      case StarterError.ResourceReadWrite(when, message) =>
        s"When $when, message: $message"

      case StarterError.GitHub(when, gitHubError) =>
        s"When $when, error: ${gitHubError.render}"

      case StarterError.Unexpected(when, error) =>
        s"When $when, error: ${error.getMessage}\n${error.show}"
    }
  }
}
