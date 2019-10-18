package kevinlee.github

import java.io.{File, IOException}
import java.net.HttpURLConnection

import javax.activation.MimetypesFileTypeMap
import javax.net.ssl.HttpsURLConnection
import kevinlee.git.Git.TagName
import kevinlee.github.data._
import org.kohsuke.github.{GHRelease, GHRepository, GitHub}

import just.fp.syntax._

/**
  * @author Kevin Lee
  * @since 2019-03-09
  */
object GitHubApi {
  val contentTypeMap: MimetypesFileTypeMap = {
    val map = new javax.activation.MimetypesFileTypeMap()
    map.addMimeTypes("application/zip jar zip")
    map.addMimeTypes("application/xml pom xml")
    map
  }

  def connectWithOAuth(oAuthToken: OAuthToken): Either[GitHubError, GitHub] =
    try {
      val github = GitHub.connectUsingOAuth(oAuthToken.token)
      if (github.isCredentialValid)
        github.right
      else
        GitHubError.invalidCredential.left
    } catch {
      case _: IllegalStateException =>
        GitHubError.noCredential.left
      case ex: IOException =>
        GitHubError.connectionFailure(ex.getMessage).left
    }

  def getRepo(gitHub: GitHub, repo: Repo): Either[GitHubError, GHRepository] =
    try {
      gitHub.getRepository(Repo.repoNameString(repo)).right
    } catch {
      case error: IOException =>
        GitHubError.gitHubServerError(error.getMessage).left
    }

  def releaseExists(gitHub: GitHub, repo: Repo, tagName: TagName): Boolean = {
    val gitHubUrl = s"${gitHub.getApiUrl}/repos/${repo.repoOrg.org}/${repo.repoName.name}/releases/tags/${tagName.value}"
    lazy val gitHubConnection = new java.net.URL(gitHubUrl).openConnection() match {
      case c: HttpsURLConnection =>
        c
      case c: HttpURLConnection =>
        c
    }
    200 === gitHubConnection.getResponseCode
  }

  def createGHRelease(gHRepository: GHRepository, tagName: TagName, changelog: Changelog): Either[GitHubError, GHRelease] = try {
    val gHRelease = gHRepository.createRelease(tagName.value)
      .body(changelog.changelog)
      .name(tagName.value)
      .create()
    gHRelease.right
  } catch {
    case throwable: Throwable =>
      GitHubError.releaseCreationError(throwable.getMessage).left
  }

  def release(
      gitHub: GitHub
    , repo: Repo
    , tagName: TagName
    , changelog: Changelog
    , assets: Seq[File]
  ): Either[GitHubError, GitHubRelease] =
    if (releaseExists(gitHub, repo, tagName)) {
      GitHubError.releaseAlreadyExists(tagName).left
    } else {
      for {
        gHRepository <- GitHubApi.getRepo(gitHub, repo).right
        release <- createGHRelease(gHRepository, tagName, changelog).right
      } yield GitHubRelease(
          tagName
        , changelog
        , assets.map { file =>
            val contentType = contentTypeMap.getContentType(file)
            release.uploadAsset(file, contentType)
            file
          }
        , release
      )
    }

}
