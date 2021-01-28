package kevinlee.http

import cats.Show
import cats.syntax.all._

/** @author Kevin Lee
  * @since 2021-01-03
  */
sealed trait HttpError

object HttpError {
  final case class InvalidUri(
    uriString: String,
    errorMessage: String,
  ) extends HttpError
  final case class ResponseBodyDecodingFailure(
    message: String,
    cause: Option[Throwable],
  ) extends HttpError
  final case class FailedResponse(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class UnhandledThrowable(
    throwable: Throwable
  ) extends HttpError
  final case class NotFound(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class BadRequest(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class InternalServerError(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class RequestTimeout(
    httpResponse: HttpResponse
  ) extends HttpError
  final case class Forbidden(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class UnprocessableEntity(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError
  final case class MethodUnsupportedForMultipart(
    httpRequest: HttpRequest
  ) extends HttpError
  final case class Unauthorized(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ) extends HttpError

  def invalidUri(uriString: String, errorMessage: String): HttpError =
    InvalidUri(uriString, errorMessage)

  def responseBodyDecodingFailure(message: String, cause: Option[Throwable]): HttpError =
    ResponseBodyDecodingFailure(message, cause)

  def failedResponse(httpResponse: HttpResponse): HttpError = FailedResponse(httpResponse)

  def unhandledThrowable(throwable: Throwable): HttpError = UnhandledThrowable(throwable)

  def notFound(
    httpRequest: HttpRequest,
    httpResponse: HttpResponse,
  ): HttpError = NotFound(httpRequest, httpResponse)

  def badRequest(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    BadRequest(httpRequest, httpResponse)

  def internalServerError(httpResponse: HttpResponse): HttpError =
    InternalServerError(httpResponse)

  def requestTimeout(httpResponse: HttpResponse): HttpError = RequestTimeout(httpResponse)

  def unauthorized(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    Unauthorized(httpRequest, httpResponse)

  def forbidden(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    Forbidden(httpRequest, httpResponse)

  def unprocessableEntity(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    UnprocessableEntity(httpRequest, httpResponse)

  def methodUnsupportedForMultipart(httpRequest: HttpRequest): HttpError =
    MethodUnsupportedForMultipart(httpRequest)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit final val show: Show[HttpError] = _.toString

  def recoverFromOptional404[A](httpError: HttpError): Either[HttpError, Option[A]] =
    httpError match {
      case HttpError.FailedResponse(
            HttpResponse(HttpResponse.Status(HttpResponse.Status.Code(404), _), _, _)
          ) =>
        none[A].asRight[HttpError]
      case error =>
        error.asLeft[Option[A]]
    }

  def toOptionIfNotFound[A](httpErrorOrA: Either[HttpError, Option[A]]): Either[HttpError, Option[A]] =
    httpErrorOrA match {
      case Right(a) =>
        a.asRight[HttpError]

      case Left(HttpError.NotFound(_, _)) =>
        none[A].asRight[HttpError]

      case Left(_) =>
        httpErrorOrA
    }

  implicit final class EitherHttpErrorOps[A](
    val httpError: Either[HttpError, Option[A]]
  ) extends AnyVal {
    def toOptionIfNotFound: Either[HttpError, Option[A]] =
      HttpError.toOptionIfNotFound(httpError)
  }
}
