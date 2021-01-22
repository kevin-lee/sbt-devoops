package kevinlee.github.data

import io.estatico.newtype.macros.newtype

import java.io.File
import kevinlee.git.Git.TagName
import kevinlee.http.HttpRequest
import org.kohsuke.github.GHRelease

/** @author Kevin Lee
  * @since 2019-03-09
  */
final case class OAuthToken(token: String) extends AnyVal {
  override def toString: String = "***Protected***"
}

final case class RepoOrg(org: String)   extends AnyVal
final case class RepoName(name: String) extends AnyVal

final case class Repo(repoOrg: RepoOrg, repoName: RepoName)
object Repo {
  def repoNameString(repo: Repo): String = s"${repo.repoOrg.org}/${repo.repoName.name}"
}

final case class Changelog(changelog: String)                 extends AnyVal
final case class ChangelogLocation(changeLogLocation: String) extends AnyVal

final case class OldGitHubRelease(
  tagName: TagName,
  changelog: Changelog,
  releasedFiles: Seq[File],
  gHRelease: GHRelease,
)

final case class GitHubRepo(
  org: GitHubRepo.Org,
  repo: GitHubRepo.Repo,
)

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  )
)
object GitHubRepo {

  @newtype case class Org(org: String)
  @newtype case class Repo(repo: String)

}

final case class GitHubRepoWithAuth(
  gitHubRepo: GitHubRepo,
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

}
