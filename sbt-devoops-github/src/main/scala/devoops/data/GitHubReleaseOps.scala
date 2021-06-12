package devoops.data

import cats.syntax.all._
import kevinlee.git.Git.{RepoUrl, TagName}
import kevinlee.github.data.{GitHub, GitHubError}
import kevinlee.sbt.io.{CaseSensitivity, Io}

import java.io.{File, FileInputStream}
import scala.io.Codec

/** @author Kevin Lee
  * @since 2021-02-14
  */
trait GitHubReleaseOps {

  def decideVersion(projectVersion: String, decide: String => String): String =
    decide(projectVersion)

  def copyFiles(
    taskName: String,
    caseSensitivity: CaseSensitivity,
    projectBaseDir: File,
    filePaths: List[String],
    targetDir: File,
  ): Either[SbtTaskError, Vector[File]] =
    scala.util.Try {
      val files  = Io.findAllFiles(
        caseSensitivity,
        projectBaseDir,
        filePaths,
      )
      val copied = Io.copy(files, targetDir)
      println(s""">> copyPackages - Files copied from:
                 |${files.mkString("  - ", "\n  - ", "\n")}
                 |  to
                 |${copied.mkString("  - ", "\n  - ", "\n")}
                 |""".stripMargin)
      copied
    } match {
      case scala.util.Success(files) =>
        files.asRight
      case scala.util.Failure(error) =>
        SbtTaskError.ioError(taskName, error).asLeft
    }

  def readOAuthToken(maybeFile: Option[File]): Either[GitHubError, GitHub.OAuthToken] =
    maybeFile match {
      case Some(file) =>
        val props = new java.util.Properties()
        props.load(new FileInputStream(file))
        Option(props.getProperty("oauth"))
          .fold[Either[GitHubError, GitHub.OAuthToken]](GitHubError.noCredential.asLeft)(token =>
            GitHub.OAuthToken(token).asRight
          )
      case None       =>
        GitHubError.noCredential.asLeft
    }

  def getRepoFromUrl(repoUrl: RepoUrl): Either[GitHubError, GitHub.Repo] = {
    val names =
      if (repoUrl.repoUrl.startsWith("http"))
        repoUrl.repoUrl.split("/")
      else
        repoUrl.repoUrl.split(":").last.split("/")
    names.takeRight(2) match {
      case Array(org, name) =>
        GitHub.Repo(GitHub.Repo.Org(org), GitHub.Repo.Name(name.stripSuffix(".git"))).asRight
      case _                =>
        GitHubError.invalidGitHubRepoUrl(repoUrl).asLeft
    }
  }

  def getChangelog(dir: File, tagName: TagName): Either[GitHubError, GitHub.Changelog] = {
    val changelogName = s"${tagName.value.stripPrefix("v")}.md"
    val changelog     = new File(dir, changelogName)
    if (!changelog.exists) {
      GitHubError.changelogNotFound(changelog.getCanonicalPath, tagName).asLeft
    } else {
      lazy val changelogSource = scala.io.Source.fromFile(changelog)(Codec.UTF8)
      try {
        val log = changelogSource.getLines().mkString("\n")
        GitHub.Changelog(log).asRight
      } finally {
        changelogSource.close()
      }
    }
  }

}
