package kevinlee.github.data

import cats.Monad
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string
import extras.cats.syntax.either._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.estatico.newtype.macros.{newsubtype, newtype}
import just.sysprocess.{ProcessError, ProcessResult, SysProcess}
import kevinlee.http.HttpRequest
import loggerf.core._
import loggerf.core.syntax.all._

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
object GitHub {

  def findRemoteRepo[F[_]: Monad: Fx: Log](): F[Option[String]] = for {
    sysProcess <- pureOf(SysProcess.singleSysProcess(none, "git", "ls-remote", "--get-url", "origin"))
    result     <- effectOf(sysProcess.run())
                    .eitherT
                    .transform {
                      case Right(ProcessResult(result)) =>
                        result.asRight[String]

                      case Left(ProcessError.Failure(code, error)) =>
                        s"Failed: code: $code, ${error.mkString("\n")}".asLeft[List[String]]

                      case Left(ProcessError.FailureWithNonFatal(nonFatalThrowable)) =>
                        nonFatalThrowable.getMessage.asLeft[List[String]]
                    }
                    .foldF(
                      err => log(pureOf(err))(debug) >> pureOf(none[String]),
                      result => pureOf(result.mkString.trim.some),
                    )
  } yield result

  def findGitHubRepoOrgAndName[F[_]: Monad: Fx](remoteRepo: String): F[Option[GitHub.Repo]] = {

    val identifier  = """([^\/]+?)"""
    val GitHubHttps = raw"""https://github.com/$identifier/$identifier(?:\.git)?""".r
    val GitHubGit   = raw"""git://github.com:$identifier/$identifier(?:\.git)?""".r
    val GitHubSsh   = raw"""git@github.com:$identifier/$identifier(?:\.git)?""".r

    for {
      result <- remoteRepo match {
                  case GitHubHttps(org, name) =>
                    pureOf(GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some)

                  case GitHubGit(org, name) =>
                    pureOf(GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some)

                  case GitHubSsh(org, name) =>
                    pureOf(GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name)).some)

                  case _ =>
                    pureOf(none[GitHub.Repo])
                }
    } yield result
  }

  final case class OAuthToken(token: String) extends AnyVal {
    override def toString: String = "***Protected***"
  }

  @newtype final case class Changelog(changelog: String)

  @newtype final case class ChangelogLocation(changeLogLocation: String)

  final case class Repo(
    org: Repo.Org,
    name: Repo.Name,
  )

  object Repo {

    @newtype final case class Org(org: String)

    @newtype final case class Name(name: String)

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
      @newtype final case class Name(name: String)
      object Name {
        implicit val encoder: Encoder[Name] = deriving
        implicit val decoder: Decoder[Name] = deriving
      }

      final case class Commit(sha: String, url: String)
      object Commit {
        @newtype final case class Sha(sha: String)
        @newtype final case class Url(url: String)

        implicit val encoder: Encoder[Commit] = deriveEncoder
        implicit val decoder: Decoder[Commit] = deriveDecoder
      }

      @newtype final case class ZipballUrl(zipballUrl: String)
      object ZipballUrl {
        implicit val encoder: Encoder[ZipballUrl] = deriving
        implicit val decoder: Decoder[ZipballUrl] = deriving
      }
      @newtype final case class TarballUrl(tarballUrl: String)
      object TarballUrl {
        implicit val encoder: Encoder[TarballUrl] = deriving
        implicit val decoder: Decoder[TarballUrl] = deriving
      }
      @newtype final case class NodeId(nodeId: String)
      object NodeId {
        implicit val encoder: Encoder[NodeId] = deriving
        implicit val decoder: Decoder[NodeId] = deriving
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

    implicit final class AccessTokenOps(val maybeAccessToken: Option[AccessToken]) extends AnyVal {
      def toHeaderList: List[HttpRequest.Header] =
        maybeAccessToken
          .toList
          .map(token =>
            HttpRequest.Header(
              "Authorization" -> s"token ${token.accessToken}",
            ),
          )
    }

    implicit final class RepoOps(val repo: GitHubRepoWithAuth) extends AnyVal {
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
    @newsubtype case class Id(id: Long)
    object Id {
      implicit val encoder: Encoder[Id] = deriving
      implicit val decoder: Decoder[Id] = deriving
    }
    @newtype final case class Login(login: String Refined NonEmpty)
    object Login {
      implicit val encoder: Encoder[Login] = deriving
      implicit val decoder: Decoder[Login] = deriving
    }
    @newtype final case class Url(url: String Refined string.Url)
    object Url {
      implicit val encoder: Encoder[Url] = deriving
      implicit val decoder: Decoder[Url] = deriving
    }
    @newtype final case class Name(name: String)
    object Name {
      implicit val encoder: Encoder[Name] = deriving
      implicit val decoder: Decoder[Name] = deriving
    }
    @newtype final case class AvatarUrl(avatarUrl: String)
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
          ): _*,
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
