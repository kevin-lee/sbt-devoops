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

  def invalidUri(uriString: String, errorMessage: String): HttpError = InvalidUri(uriString, errorMessage)

  def responseBodyDecodingFailure(message: String, cause: Option[Throwable]): HttpError =
    ResponseBodyDecodingFailure(message, cause)

  def failedResponse(httpResponse: HttpResponse): HttpError = FailedResponse(httpResponse)

  def unhandledThrowable(throwable: Throwable): HttpError = UnhandledThrowable(throwable)

  def badRequest(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    BadRequest(httpRequest, httpResponse)

  def internalServerError(httpResponse: HttpResponse): HttpError =
    InternalServerError(httpResponse)

  def requestTimeout(httpResponse: HttpResponse): HttpError = RequestTimeout(httpResponse)

  def recoverFromOptional404[A](httpError: HttpError): Either[HttpError, Option[A]] = httpError match {
    case HttpError.FailedResponse(
          HttpResponse(HttpResponse.Status(HttpResponse.Status.Code(404), _), _, _)
        ) =>
      none[A].asRight[HttpError]
    case error =>
      error.asLeft[Option[A]]
  }

  def forbidden(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError = Forbidden(httpRequest, httpResponse)

  def unprocessableEntity(httpRequest: HttpRequest, httpResponse: HttpResponse): HttpError =
    UnprocessableEntity(httpRequest, httpResponse)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit final val show: Show[HttpError] = _.toString

}
