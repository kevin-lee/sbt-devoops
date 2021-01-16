package kevinlee.github

import cats._
import cats.data.EitherT
import cats.implicits._
import effectie.cats.Catching._
import effectie.cats.Effectful._
import effectie.cats.EitherTSupport._
import effectie.cats.{EffectConstructor, _}
import kevinlee.git.Git.TagName
import kevinlee.github.data._
import org.kohsuke.github.{GHRelease, GHRepository, GitHub}

import java.io.{File, IOException}
import java.net.{HttpURLConnection, MalformedURLException, URLConnection}
import javax.activation.MimetypesFileTypeMap
import javax.net.ssl.HttpsURLConnection
import scala.util.Try
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2019-03-09
  */
trait OldGitHubApi[F[_]] {

  def connectWithOAuth(oAuthToken: OAuthToken): F[Either[GitHubError, GitHub]]

  def getRepo(gitHub: GitHub, repo: Repo): F[Either[GitHubError, GHRepository]]

  def releaseExists(gitHub: GitHub, repo: Repo, tagName: TagName): F[Either[GitHubError, Boolean]]

  def createGHRelease(
    gHRepository: GHRepository,
    tagName: TagName,
    changelog: Changelog
  ): F[Either[GitHubError, GHRelease]]

  def release(
    gitHub: GitHub,
    repo: Repo,
    tagName: TagName,
    changelog: Changelog,
    assets: Seq[File]
  ): F[Either[GitHubError, GitHubRelease]]
}

object OldGitHubApi {

  def apply[F[_]: OldGitHubApi]: OldGitHubApi[F] = implicitly[OldGitHubApi[F]]

  implicit def oldGitHubApiF[F[_]: EffectConstructor: CanCatch: Monad]: OldGitHubApi[F] = new OldGitHubApiF[F]

  final class OldGitHubApiF[F[_]: EffectConstructor: CanCatch: Monad] extends OldGitHubApi[F] {
    val contentTypeMap: MimetypesFileTypeMap = {
      val map = new javax.activation.MimetypesFileTypeMap()
      map.addMimeTypes("application/zip jar zip")
      map.addMimeTypes("application/xml pom xml")
      map
    }

    override def connectWithOAuth(oAuthToken: OAuthToken): F[Either[GitHubError, GitHub]] =
      eitherTOf[F](Either.fromTry(Try(GitHub.connectUsingOAuth(oAuthToken.token)))).transform {
        case Right(github)                  =>
          if (github.isCredentialValid)
            github.asRight[GitHubError]
          else
            GitHubError.invalidCredential.asLeft[GitHub]
        case Left(_: IllegalStateException) =>
          GitHubError.noCredential.asLeft[GitHub]
        case Left(ex)                       =>
          GitHubError.connectionFailure(ex.getMessage).asLeft[GitHub]
      }.value

    override def getRepo(gitHub: GitHub, repo: Repo): F[Either[GitHubError, GHRepository]] =
      try {
        effectOf(gitHub.getRepository(Repo.repoNameString(repo))).map(_.asRight[GitHubError])
      } catch {
        case error: IOException =>
          pureOf(GitHubError.gitHubServerError(error.getMessage).asLeft)
      }

    override def releaseExists(gitHub: GitHub, repo: Repo, tagName: TagName): F[Either[GitHubError, Boolean]] = (for {
      gitHubUrl <- eitherTRightPure[GitHubError](
                     s"${gitHub.getApiUrl}/repos/${repo.repoOrg.org}/${repo.repoName.name}/releases/tags/${tagName.value}"
                   )
      url       <- eitherTOf(Try(new java.net.URL(gitHubUrl)).toEither.leftMap {
                     case ex: MalformedURLException =>
                       GitHubError.malformedURL(gitHubUrl, Option(ex.getLocalizedMessage))

                   })
      conn      <- EitherT(catchNonFatal[F][URLConnection](effectOf[F](url.openConnection())) {
                     case NonFatal(err: IOException) =>
                       GitHubError.connectionFailure(err.getLocalizedMessage)

                   })
      gitHubConn =
        conn match {
          case c: HttpsURLConnection =>
            c
          case c: HttpURLConnection  =>
            c
        }
    } yield 200 === gitHubConn.getResponseCode).value

    override def createGHRelease(
      gHRepository: GHRepository,
      tagName: TagName,
      changelog: Changelog
    ): F[Either[GitHubError, GHRelease]] =
      catchNonFatal(
        effectOf[F](
          gHRepository
            .createRelease(tagName.value)
            .body(changelog.changelog)
            .name(tagName.value)
            .create()
        )
      ) {
        case throwable: Throwable =>
          GitHubError.releaseCreationError(throwable.getMessage)

      }

    override def release(
      gitHub: GitHub,
      repo: Repo,
      tagName: TagName,
      changelog: Changelog,
      assets: Seq[File]
    ): F[Either[GitHubError, GitHubRelease]] = (for {
      exists        <- EitherT(releaseExists(gitHub, repo, tagName))
      gitHubRelease <-
        if (exists) {
          eitherTLeftPure[GitHubRelease](GitHubError.releaseAlreadyExists(tagName))
        } else {
          for {
            gHRepository <- EitherT(this.getRepo(gitHub, repo))
            release      <- EitherT(createGHRelease(gHRepository, tagName, changelog))
          } yield GitHubRelease(
            tagName,
            changelog,
            assets.map { file =>
              val contentType = contentTypeMap.getContentType(file)
              release.uploadAsset(file, contentType)
              file
            },
            release
          )
        }
    } yield gitHubRelease).value

  }

}
