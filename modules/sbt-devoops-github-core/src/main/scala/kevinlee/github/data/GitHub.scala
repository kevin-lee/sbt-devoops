package kevinlee.github.data

import cats.Monad
import cats.syntax.all.*
import effectie.core.*
import effectie.syntax.all.*
import extras.cats.syntax.either.*
import just.sysprocess.{ProcessError, ProcessResult, SysProcess}
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
object GitHub extends GitHubBase {

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
                      err => err.logS_(debug) *> pureOf(none[String]),
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


}
