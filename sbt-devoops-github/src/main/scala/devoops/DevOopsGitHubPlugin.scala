package devoops

import cats.effect.IO
import kevinlee.github.data.GitHub
import loggerf.logger.{CanLog, SbtLogger}
import sbt.Keys.streams
import sbt._
import extras.cats.syntax.option._

/** @author Kevin Lee
  * @since 2021-09-18
  */
object DevOopsGitHubPlugin extends AutoPlugin {

  override def requires: Plugins      = empty
  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    lazy val gitHubFindRepoOrgAndName: TaskKey[Option[(String, String)]] = taskKey[Option[(String, String)]](
      "Try to get GitHub Org (username) and Name (repo name). It returns Option of (Org, Name)."
    )

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    gitHubFindRepoOrgAndName := {
      implicit val log: CanLog = SbtLogger.sbtLoggerCanLog(streams.value.log)
      (for {
        remoteRepo <- GitHub.findRemoteRepo[IO]().optionT
        repo       <- GitHub.findGitHubRepoOrgAndName[IO](remoteRepo).optionT
      } yield (repo.org.org, repo.name.name))
        .value
        .unsafeRunSync()
    }
  )
}
