package kevinlee.github.data

import kevinlee.git.Git.{RepoUrl, TagName}
import kevinlee.git.GitCommandError

/**
  * @author Kevin Lee
  * @since 2019-03-09
  */
sealed trait GitHubError

object GitHubError {
  final case object NoCredential extends GitHubError
  final case object InvalidCredential extends GitHubError
  final case class ConnectionFailure(error: String) extends GitHubError
  final case class GitHubServerError(error: String) extends GitHubError
  final case class ReleaseAlreadyExists(tagName: TagName) extends GitHubError
  final case class ReleaseCreationError(message: String) extends  GitHubError
  final case class InvalidGitHubRepoUrl(repoUrl: RepoUrl) extends GitHubError
  final case class ChangelogNotFound(changelogLocation: String, tagName: TagName) extends GitHubError
  final case class CausedByGitCommandError(cause: GitCommandError) extends GitHubError

  def noCredential: GitHubError = NoCredential

  def invalidCredential: GitHubError = InvalidCredential

  def connectionFailure(error: String): GitHubError =
    ConnectionFailure(error)

  def gitHubServerError(error: String): GitHubError = GitHubServerError(error)

  def releaseAlreadyExists(tagName: TagName): GitHubError =
    ReleaseAlreadyExists(tagName)

  def releaseCreationError(message: String): GitHubError =
    ReleaseCreationError(message)

  def invalidGitHubRepoUrl(repoUrl: RepoUrl): GitHubError =
    InvalidGitHubRepoUrl(repoUrl)

  def changelogNotFound(changelogLocation: String, tagName: TagName): GitHubError =
    ChangelogNotFound(changelogLocation, tagName)

  def causedByGitCommandError(cause: GitCommandError): GitHubError =
    CausedByGitCommandError(cause)

  def render(gitHubError: GitHubError): String = gitHubError match {
    case NoCredential =>
      "No GitHub access credential found"

    case InvalidCredential =>
      "Invalid GitHub access credential"

    case ConnectionFailure(error) =>
      s"GitHub API connection failed - error: $error"

    case GitHubServerError(error) =>
      s"GitHub server error - error: $error"

    case ReleaseAlreadyExists(tagName)  =>
      s"Error] The release with the given tag name (${tagName.value}) already exists on GitHub."

    case ReleaseCreationError(message) =>
      s"Error] Failed to create GitHub release - reason: $message"

    case InvalidGitHubRepoUrl(repoUrl) =>
      s"Error] Invalid GitHub repository URL: ${repoUrl.repoUrl}"

    case ChangelogNotFound(changelogLocation, tagName) =>
      s"Changelog file does not exist at $changelogLocation for the tag, ${tagName.value}"

    case CausedByGitCommandError(cause) =>
      s"""Error] GitHub task failed due to git command error:
         |  ${GitCommandError.render(cause)}
         |""".stripMargin

  }
}
