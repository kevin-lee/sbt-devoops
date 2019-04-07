package kevinlee.github

import java.io.{File, IOException}
import java.net.HttpURLConnection

import javax.activation.MimetypesFileTypeMap
import javax.net.ssl.HttpsURLConnection
import kevinlee.git.Git.TagName
import kevinlee.github.data._
import kevinlee.CommonPredef._
import org.kohsuke.github.{GHRelease, GHRepository, GitHub}


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
        Right(github)
      else
        Left(GitHubError.invalidCredential)
    } catch {
      case _: IllegalStateException =>
        Left(GitHubError.noCredential)
      case ex: IOException =>
        Left(GitHubError.connectionFailure(ex.getMessage))
    }

  def getRepo(gitHub: GitHub, repo: Repo): Either[GitHubError, GHRepository] =
    try {
      Right(gitHub.getRepository(Repo.repoNameString(repo)))
    } catch {
      case error: IOException =>
        Left(GitHubError.gitHubServerError(error.getMessage))
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
    Right(gHRelease)
  } catch {
    case throwable: Throwable =>
      Left(GitHubError.releaseCreationError(throwable.getMessage))
  }

  def release(
      gitHub: GitHub
    , repo: Repo
    , tagName: TagName
    , changelog: Changelog
    , assets: Seq[File]
  ): Either[GitHubError, GitHubRelease] =
    if (releaseExists(gitHub, repo, tagName)) {
      Left(GitHubError.releaseAlreadyExists(tagName))
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
