package kevinlee.git

import cats._
import cats.data._
import cats.syntax.all._
import effectie.syntax.all._
import effectie.core._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

import java.io.File

/** @author Kevin Lee
  * @since 2019-01-01
  */
trait Git[F[_]] {
  import Git._

  def fromProcessResultToEither[A](
    gitCmd: GitCmd,
    successHandler: List[String] => A,
    errorHandler: (GitCmd, Int, List[String]) => GitCommandError,
  ): PartialFunction[ProcessResult, Either[GitCommandError, (GitCommandResult, A)]]

  def git(baseDir: File, commandAndArgs: List[String]): ProcessResult

  def git1(baseDir: File, command: String, args: String*): ProcessResult

  def gitCmd[A](
    baseDir: File,
    gitCmd: GitCmd,
    f: List[String] => A,
    e: (GitCmd, Int, List[String]) => GitCommandError,
  ): F[Either[GitCommandError, (GitCommandResult, A)]]

  def gitCmdSimple[A](
    baseDir: File,
    cmd: GitCmd,
    resultHandler: List[String] => A,
  ): F[Either[GitCommandError, (GitCommandResult, A)]]

  def gitCmdSimpleWithWriter[A](
    baseDir: File,
    cmd: GitCmd,
    resultHandler: List[String] => A,
  ): CmdResult[F, A]

  def updateHistory[A](
    gitCmd: GitCmd,
    r: F[Either[GitCommandError, (GitCommandResult, A)]],
  ): CmdResult[F, A]

  def currentBranchName(baseDir: File): CmdResult[F, BranchName]

  def checkIfCurrentBranchIsSame(
    branchName: BranchName,
    baseDir: File,
  ): CmdResult[F, Boolean]

  def checkout(branchName: BranchName, baseDir: File): CmdResult[F, Unit]

  def fetchTags(baseDir: File): CmdResult[F, List[String]]

  def getTag(baseDir: File): CmdResult[F, List[String]]

  def tag(tagName: TagName, baseDir: File): CmdResult[F, TagName]

  def tagWithDescription(
    tagName: TagName,
    description: Description,
    baseDir: File,
  ): CmdResult[F, TagName]

  def pushTag(
    repository: Repository,
    tagName: TagName,
    baseDir: File,
  ): CmdResult[F, List[String]]

  def getRemoteUrl(repository: Repository, baseDir: File): CmdResult[F, RepoUrl]

}

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  )
)
object Git {
  // $COVERAGE-OFF$

  type CmdHistory = List[GitCmdAndResult]

  type CmdHistoryWriter[F[_], A] = WriterT[F, CmdHistory, A]

  type CmdResult[F[_], A] = EitherT[CmdHistoryWriter[F, *], GitCommandError, A]

  @newtype final case class BranchName(value: String)
  @newtype final case class TagName(value: String)
  object TagName {
    implicit val encoder: Encoder[TagName] = deriving
    implicit val decoder: Decoder[TagName] = deriving
  }
  @newtype final case class Repository(value: String)
  @newtype final case class RemoteName(remoteName: String)
  @newtype final case class RepoUrl(repoUrl: String)
  @newtype final case class Description(value: String)

  @newtype final case class TagMessage(tagMessage: String)
  @newtype final case class HashObject(hashObject: String)

  def apply[F[_]: Git]: Git[F] = implicitly[Git[F]]

  implicit def gitF[F[_]: Fx: Monad]: Git[F] = new GitF[F]

  final class GitF[F[_]: Fx: Monad] extends Git[F] {

    override def fromProcessResultToEither[A](
      gitCmd: GitCmd,
      successHandler: List[String] => A,
      errorHandler: (GitCmd, Int, List[String]) => GitCommandError,
    ): PartialFunction[ProcessResult, Either[GitCommandError, (GitCommandResult, A)]] = {
      case ProcessResult.Success(outputs) =>
        (GitCommandResult.genericResult(outputs), successHandler(outputs)).asRight

      case ProcessResult.Failure(code, errors) =>
        errorHandler(gitCmd, code, errors).asLeft
    }

    override def git(baseDir: File, commandAndArgs: List[String]): ProcessResult =
      SysProcess.run(
        SysProcess.process(Some(baseDir), "git" :: commandAndArgs)
      )

    override def git1(baseDir: File, command: String, args: String*): ProcessResult =
      git(baseDir, command :: args.toList)

    override def gitCmd[A](
      baseDir: File,
      gitCmd: GitCmd,
      f: List[String] => A,
      e: (GitCmd, Int, List[String]) => GitCommandError,
    ): F[Either[GitCommandError, (GitCommandResult, A)]] = for {
      gitCmdAndArgs <- pureOf(GitCmd.cmdAndArgs(gitCmd))
      errorOrResult <- effectOf(
                         ProcessResult.toEither(
                           git(baseDir, gitCmdAndArgs)
                         )(
                           fromProcessResultToEither(gitCmd, f, e)
                         )
                       )
    } yield errorOrResult

    override def gitCmdSimple[A](
      baseDir: File,
      cmd: GitCmd,
      resultHandler: List[String] => A,
    ): F[Either[GitCommandError, (GitCommandResult, A)]] =
      gitCmd(
        baseDir,
        cmd,
        resultHandler,
        GitCommandError.genericGotCommandResultError,
      )

    override def gitCmdSimpleWithWriter[A](
      baseDir: File,
      cmd: GitCmd,
      resultHandler: List[String] => A,
    ): CmdResult[F, A] =
      updateHistory(
        cmd,
        gitCmdSimple(
          baseDir,
          cmd,
          resultHandler,
        ),
      )

    override def updateHistory[A](
      gitCmd: GitCmd,
      r: F[Either[GitCommandError, (GitCommandResult, A)]],
    ): CmdResult[F, A] =
      EitherT {
        val fOf = r.map { eth =>
          val w: CmdHistory                    =
            eth match {
              case Left(error) =>
                List.empty[GitCmdAndResult]
              case Right((cmdResult, a)) =>
                List(GitCmdAndResult(gitCmd, cmdResult))
            }
          val eth2: Either[GitCommandError, A] = eth.map {
            case (_, a) =>
              a
          }
          (w, eth2)
        }
        WriterT(fOf)
      }

    override def currentBranchName(baseDir: File): CmdResult[F, BranchName] =
      gitCmdSimpleWithWriter[BranchName](
        baseDir,
        GitCmd.currentBranchName,
        xs => BranchName(xs.mkString.trim),
      )

    override def checkIfCurrentBranchIsSame(
      branchName: BranchName,
      baseDir: File,
    ): CmdResult[F, Boolean] = for {
      current <- currentBranchName(baseDir)
    } yield current.value === branchName.value

    override def checkout(branchName: BranchName, baseDir: File): CmdResult[F, Unit] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.checkout(branchName),
        _ => (),
      )

    override def fetchTags(baseDir: File): CmdResult[F, List[String]] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.fetchTags,
        identity,
      )

    def getTag(baseDir: File): CmdResult[F, List[String]] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.getTag,
        identity,
      )

    override def tag(tagName: TagName, baseDir: File): CmdResult[F, TagName] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.tag(tagName),
        _ => tagName,
      )

    override def tagWithDescription(
      tagName: TagName,
      description: Description,
      baseDir: File,
    ): CmdResult[F, TagName] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.tagWithDescription(tagName, description),
        _ => tagName,
      )

    override def pushTag(
      repository: Repository,
      tagName: TagName,
      baseDir: File,
    ): CmdResult[F, List[String]] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.push(repository, tagName),
        identity,
      )

    override def getRemoteUrl(repository: Repository, baseDir: File): CmdResult[F, RepoUrl] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.remoteGetUrl(repository),
        xs => RepoUrl(xs.mkString.trim),
      )
  }
}
