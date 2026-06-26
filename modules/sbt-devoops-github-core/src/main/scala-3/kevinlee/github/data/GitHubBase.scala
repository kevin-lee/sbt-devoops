package kevinlee.github.data

import cats.Monad
import cats.syntax.all.*
import effectie.core.*
import effectie.syntax.all.*
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string
import extras.cats.syntax.either.*
import io.circe.generic.semiauto.*
import io.circe.refined.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import refined4s.*
import refined4s.modules.circe.derivation.CirceNewtypeCodec
import just.sysprocess.{ProcessError, ProcessResult, SysProcess}
import kevinlee.http.HttpRequest
import loggerf.core.*
import loggerf.core.syntax.all.*

/** @author Kevin Lee
  * @since 2019-03-09
  */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  ),
)
trait GitHubBase {
  type Changelog = GitHubBase.Changelog
  val Changelog = GitHubBase.Changelog

  type ChangelogLocation = GitHubBase.ChangelogLocation
  val ChangelogLocation = GitHubBase.ChangelogLocation

  type Repo = GitHubBase.Repo
  val Repo = GitHubBase.Repo

  type GitHubRepoWithAuth = GitHubBase.GitHubRepoWithAuth
  val GitHubRepoWithAuth = GitHubBase.GitHubRepoWithAuth

  type User = GitHubBase.User
  val User = GitHubBase.User
}
object GitHubBase {

  type Changelog = Changelog.Type
  object Changelog extends Newtype[String] {
    extension (a: Changelog) def changelog: String = a.value
  }

  type ChangelogLocation = ChangelogLocation.Type
  object ChangelogLocation extends Newtype[String] {
    extension (a: ChangelogLocation) def changeLogLocation: String = a.value
  }

  final case class Repo(
    org: Repo.Org,
    name: Repo.Name,
  )
  object Repo {

    type Org = Org.Type
    object Org extends Newtype[String] {
      extension (a: Org) def org: String = a.value
    }

    type Name = Name.Type
    object Name extends Newtype[String] {
      extension (a: Name) def name: String = a.value
    }

    implicit final class RepoOps(private val repo: Repo) extends AnyVal {
      def toRepoNameString: String = s"${repo.org.org}/${repo.name.name}"

      def toTupleOfString: (String, String) = (repo.org.org, repo.name.name)

      def orgToString: String  = repo.org.org
      def nameToString: String = repo.name.name
    }

    final case class Tag(
      name: Tag.Name,
      commit: Tag.Commit,
      zipballUrl: Tag.ZipballUrl,
      tarballUrl: Tag.TarballUrl,
      nodeId: Tag.NodeId,
    )
    object Tag {
      type Name = Name.Type
      object Name extends Newtype[String], CirceNewtypeCodec[String] {
        extension (a: Name) def name: String = a.value
      }

      final case class Commit(sha: String, url: String)
      object Commit {
        type Sha = Sha.Type
        object Sha extends Newtype[String] {
          extension (a: Sha) def sha: String = a.value
        }

        type Url = Url.Type
        object Url extends Newtype[String] {
          extension (a: Url) def url: String = a.value
        }

        implicit val encoder: Encoder[Commit] = deriveEncoder
        implicit val decoder: Decoder[Commit] = deriveDecoder
      }

      type ZipballUrl = ZipballUrl.Type
      object ZipballUrl extends Newtype[String], CirceNewtypeCodec[String] {
        extension (a: ZipballUrl) def zipballUrl: String = a.value
      }

      type TarballUrl = TarballUrl.Type
      object TarballUrl extends Newtype[String], CirceNewtypeCodec[String] {
        extension (a: TarballUrl) def tarballUrl: String = a.value
      }

      type NodeId = NodeId.Type
      object NodeId extends Newtype[String], CirceNewtypeCodec[String] {
        extension (a: NodeId) def nodeId: String = a.value
      }

      implicit final val encoder: Encoder[Tag] =
        tag =>
          Json.obj(
            "name"        -> tag.name.asJson,
            "commit"      -> tag.commit.asJson,
            "zipball_url" -> tag.zipballUrl.asJson,
            "tarball_url" -> tag.tarballUrl.asJson,
            "node_id"     -> tag.nodeId.asJson,
          )

      implicit final val decoder: Decoder[Tag] =
        (c: HCursor) =>
          for {
            name       <- c.downField("name").as[Name]
            commit     <- c.downField("commit").as[Commit]
            zipballUrl <- c.downField("zipball_url").as[ZipballUrl]
            tarballUrl <- c.downField("tarball_url").as[TarballUrl]
            nodeId     <- c.downField("node_id").as[NodeId]
          } yield Tag(name, commit, zipballUrl, tarballUrl, nodeId)

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

    object AccessToken {
      extension(maybeAccessToken: Option[AccessToken]) {
        def toHeaderList: List[HttpRequest.Header] =
          maybeAccessToken
            .toList
            .map(token =>
              HttpRequest.Header(
                "Authorization" -> s"token ${token.accessToken}",
              ),
            )
      }
    }

    extension (repo: GitHubRepoWithAuth) {
      def toRepoNameString: String = repo.gitHubRepo.toRepoNameString
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
    type Id = Id.Type
    object Id extends Newtype[Long], CirceNewtypeCodec[Long] {
      extension (a: Id) def id: Long = a.value
    }

    type Login = Login.Type
    object Login extends Newtype[String Refined NonEmpty], CirceNewtypeCodec[String Refined NonEmpty] {
      extension (a: Login) def login: String Refined NonEmpty = a.value
    }

    type Url = Url.Type
    object Url extends Newtype[String Refined string.Url], CirceNewtypeCodec[String Refined string.Url] {
      extension (a: Url) def url: String Refined string.Url = a.value
    }

    type Name = Name.Type
    object Name extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: Name) def name: String = a.value
    }

    type AvatarUrl = AvatarUrl.Type
    object AvatarUrl extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: AvatarUrl) def avatarUrl: String = a.value
    }

    given encoder: Encoder[User] =
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
          )*,
        )
    given decoder: Decoder[User] =
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
