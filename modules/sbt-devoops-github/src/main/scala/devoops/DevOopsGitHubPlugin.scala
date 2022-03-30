package devoops

import cats.effect.IO
import devoops.data.{DevOopsLogLevel, Logging}
import extras.cats.syntax.option._
import kevinlee.github.data.GitHub
import loggerf.logger.{CanLog, SbtLogger}
import sbt.Keys._
import sbt._

/** @author Kevin Lee
  * @since 2021-09-18
  */
object DevOopsGitHubPlugin extends AutoPlugin {

  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val gitHubFindRepoOrgAndName: TaskKey[Option[(String, String)]] = taskKey[Option[(String, String)]](
      "Try to get GitHub Org (username) and Name (repo name). It returns Option of (Org, Name).",
    )

    def findRepoOrgAndName: Option[GitHub.Repo] =
      findRepoOrgAndNameWithPrintlnLog(none)

    def findRepoOrgAndNameWithLog(logLevel: DevOopsLogLevel): Option[GitHub.Repo] =
      findRepoOrgAndNameWithPrintlnLog(logLevel.some)

  }

  private def findRepoOrgAndNameWithPrintlnLog(logLevel: Option[DevOopsLogLevel]): Option[GitHub.Repo] =
    findRepoOrgAndNameWithCanLog(Logging.printlnCanLog(logLevel))

  private def findRepoOrgAndNameWithCanLog(canLog: CanLog): Option[GitHub.Repo] = {
    implicit val log: CanLog = canLog
    (for {
      remoteRepo <- GitHub.findRemoteRepo[IO]().optionT
      repo       <- GitHub.findGitHubRepoOrgAndName[IO](remoteRepo).optionT
    } yield repo)
      .value
      .unsafeRunSync()
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    gitHubFindRepoOrgAndName :=
      findRepoOrgAndNameWithCanLog(SbtLogger.sbtLoggerCanLog(streams.value.log))
        .map {
          case GitHub.Repo(org, name) => (org.org, name.name)
        },
  )

}
