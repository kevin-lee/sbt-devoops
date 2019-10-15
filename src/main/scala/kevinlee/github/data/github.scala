package kevinlee.github.data

import java.io.File

import kevinlee.git.Git.TagName
import org.kohsuke.github.GHRelease

/**
  * @author Kevin Lee
  * @since 2019-03-09
  */
final case class OAuthToken(token: String) extends AnyVal {
  override def toString: String = "***Protected***"
}

final case class RepoOrg(org: String) extends AnyVal
final case class RepoName(name: String) extends AnyVal

final case class Repo(repoOrg: RepoOrg, repoName: RepoName)
object Repo {
  def repoNameString(repo: Repo): String = s"${repo.repoOrg.org}/${repo.repoName.name}"
}

final case class Changelog(changelog: String) extends AnyVal
final case class ChangelogLocation(changeLogLocation: String) extends AnyVal

final case class GitHubRelease(
  tagName: TagName
, changelog: Changelog
, releasedFiles: Seq[File]
, gHRelease: GHRelease
)
