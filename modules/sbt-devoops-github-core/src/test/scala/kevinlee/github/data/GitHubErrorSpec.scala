package kevinlee.github.data

import cats.syntax.all.*
import hedgehog.*
import hedgehog.runner.*
import kevinlee.http.{HttpRequest, HttpResponse}
import kevinlee.http.HttpResponse.FailedResponseBodyJson

/** @author Kevin Lee
  * @since 2026-02-22
  */
object GitHubErrorSpec extends Properties {
  override def tests: List[Test] = List(
    example(
      "isReleaseTagNameAlreadyExists returns true for Release/tag_name/already_exists",
      testIsReleaseTagNameAlreadyExists
    ),
    example(
      "isReleaseTagNameAlreadyExists returns false for non matching validation error",
      testIsReleaseTagNameAlreadyExistsWithNonMatchingError
    ),
    example(
      "isReleaseTagNameAlreadyExists returns false for non UnprocessableEntity",
      testIsReleaseTagNameAlreadyExistsWithDifferentErrorType
    ),
  )

  def testIsReleaseTagNameAlreadyExists: Result = {
    val responseBodyJson = FailedResponseBodyJson(
      "Validation Failed",
      List(
        FailedResponseBodyJson.Errors(
          Map(
            "resource" -> "Release",
            "code"     -> "already_exists",
            "field"    -> "tag_name",
          )
        )
      ),
      "https://docs.github.com/rest/releases/releases#create-a-release".some,
    )

    val error = GitHubError.unprocessableEntity(request, response, responseBodyJson.some)
    GitHubError.isReleaseTagNameAlreadyExists(error) ==== true
  }

  def testIsReleaseTagNameAlreadyExistsWithNonMatchingError: Result = {
    val responseBodyJson = FailedResponseBodyJson(
      "Validation Failed",
      List(
        FailedResponseBodyJson.Errors(
          Map(
            "resource" -> "Release",
            "code"     -> "invalid",
            "field"    -> "tag_name",
          )
        )
      ),
      "https://docs.github.com/rest/releases/releases#create-a-release".some,
    )

    val error = GitHubError.unprocessableEntity(request, response, responseBodyJson.some)
    GitHubError.isReleaseTagNameAlreadyExists(error) ==== false
  }

  def testIsReleaseTagNameAlreadyExistsWithDifferentErrorType: Result = {
    val error = GitHubError.authFailure("bad token")
    GitHubError.isReleaseTagNameAlreadyExists(error) ==== false
  }

  private val request = HttpRequest.withHeaders(
    HttpRequest.Method.post,
    HttpRequest.Uri("https://api.github.com/repos/org/repo/releases"),
    List.empty[HttpRequest.Header],
  )

  private val response = HttpResponse(
    HttpResponse.Status(HttpResponse.Status.Code(422), HttpResponse.Status.Reason("Unprocessable Content")),
    Vector.empty[HttpResponse.Header],
    HttpResponse.Body("{}").some,
  )
}
