package kevinlee.github

import cats.Monad
import cats.syntax.all._
import io.circe._
import kevinlee.github.data._
import kevinlee.http.{HttpClient, HttpRequest}

/** @author Kevin Lee
  * @since 2021-01-14
  */
trait GitHubApi[F[_]] {
  def createRelease(
    params: GitHubRelease.RequestParams,
    repo: GitHubRepoWithAuth,
  ): F[Either[GitHubError, Option[GitHubRelease.Response]]]
}

object GitHubApi {

  def apply[F[_]: Monad](httpClient: HttpClient[F]): GitHubApi[F] = new GitHubApiF[F](httpClient)

  final class GitHubApiF[F[_]: Monad](val httpClient: HttpClient[F]) extends GitHubApi[F] {
    // TODO: make it configurable
    val baseUrl: String = "https://api.github.com"

    val DefaultAccept: String = "application/vnd.github.v3+json"

    override def createRelease(
      params: GitHubRelease.RequestParams,
      repo: GitHubRepoWithAuth,
    ): F[Either[GitHubError, Option[GitHubRelease.Response]]] = {
      val url         = s"$baseUrl/repos/${repo.gitHubRepo.org.org}/${repo.gitHubRepo.repo.repo}/releases"
      val body        = Encoder[GitHubRelease.RequestParams].apply(params)
      val httpRequest = HttpRequest.withHeadersAndBody(
        HttpRequest.Method.post,
        HttpRequest.Uri(url),
        HttpRequest.Header("accept" -> DefaultAccept) ::
          repo
            .accessToken
            .toHeaderList,
        body,
      )
      httpClient
        .request[Option[GitHubRelease.Response]](httpRequest)
        .map(
          _.leftMap(GitHubError.fromHttpError)
            .flatMap(res => res.asRight[GitHubError])
        )
    }
  }

}
