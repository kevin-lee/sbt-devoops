package kevinlee.github.data

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string
import io.circe.refined._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.estatico.newtype.macros.{newsubtype, newtype}
import kevinlee.http.HttpRequest

/** @author Kevin Lee
  * @since 2019-03-09
  */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  )
)
object GitHub {

  final case class OAuthToken(token: String) extends AnyVal {
    override def toString: String = "***Protected***"
  }

  @newtype case class Changelog(changelog: String)

  @newtype case class ChangelogLocation(changeLogLocation: String)

  final case class Repo(
    org: Repo.Org,
    name: Repo.Name,
  )

  object Repo {

    @newtype case class Org(org: String)

    @newtype case class Name(name: String)

    def repoNameString(repo: Repo): String = s"${repo.org.org}/${repo.name.name}"

    implicit final class RepoOps(val repo: Repo) extends AnyVal {
      def toRepoNameString: String = Repo.repoNameString(repo)
    }

  }

  final case class GitHubRepoWithAuth(
    gitHubRepo: Repo,
    accessToken: Option[GitHubRepoWithAuth.AccessToken],
  )

  object GitHubRepoWithAuth {

    final case class AccessToken(accessToken: String) {
      override val toString: String = "***Protected***"
    }

    implicit final class AccessTokenOps(val maybeAccessToken: Option[AccessToken]) extends AnyVal {
      def toHeaderList: List[HttpRequest.Header] =
        maybeAccessToken
          .toList
          .map(token =>
            HttpRequest.Header(
              "Authorization" -> s"token ${token.accessToken}"
            )
          )
    }

    implicit final class RepoOps(val repo: GitHubRepoWithAuth) extends AnyVal {
      def toRepoNameString: String = Repo.repoNameString(repo.gitHubRepo)
    }

  }

  final case class User(
    id: User.Id,
    login: User.Login,
    url: User.Url,
    name: Option[User.Name],
    avatarUrl: Option[User.AvatarUrl],
  )

  object User {
    @newsubtype case class Id(id: Long)
    object Id        {
      implicit val encoder: Encoder[Id] = deriving
      implicit val decoder: Decoder[Id] = deriving
    }
    @newtype case class Login(login: String Refined NonEmpty)
    object Login     {
      implicit val encoder: Encoder[Login] = deriving
      implicit val decoder: Decoder[Login] = deriving
    }
    @newtype case class Url(url: String Refined string.Url)
    object Url       {
      implicit val encoder: Encoder[Url] = deriving
      implicit val decoder: Decoder[Url] = deriving
    }
    @newtype case class Name(name: String)
    object Name      {
      implicit val encoder: Encoder[Name] = deriving
      implicit val decoder: Decoder[Name] = deriving
    }
    @newtype case class AvatarUrl(avatarUrl: String)
    object AvatarUrl {
      implicit val encoder: Encoder[AvatarUrl] = deriving
      implicit val decoder: Decoder[AvatarUrl] = deriving
    }

    implicit final val encoder: Encoder[User] =
      author =>
        Json.obj(
          (
            List(
              "id"    -> author.id.asJson,
              "login" -> author.login.asJson,
              "url"   -> author.url.asJson,
            ) ++
              author
                .name
                .toList
                .map(name => "name" -> name.asJson) ++
              author
                .avatarUrl
                .toList
                .map(avatarUrl => "avatar_url" -> avatarUrl.asJson)
          ): _*
        )
    implicit final val decoder: Decoder[User] =
      (c: HCursor) =>
        for {
          id        <- c.downField("id").as[Id]
          login     <- c.downField("login").as[Login]
          url       <- c.downField("url").as[Url]
          name      <- c.downField("name").as[Option[Name]]
          avatarUrl <- c.downField("avatar_url").as[Option[AvatarUrl]]
        } yield User(id, login, url, name, avatarUrl)

  }

}
