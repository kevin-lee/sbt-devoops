package kevinlee.git

import java.io.File

import kevinlee.CommonPredef._
import kevinlee.fp._
import kevinlee.fp.Implicits._

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object Git {
  // $COVERAGE-OFF$

  type CmdHistory = List[GitCmdAndResult]

  type CmdHistoryWriter[A] = Writer[CmdHistory, A]

  type CmdResult[A] = EitherT[CmdHistoryWriter, GitCommandError, A]

  final case class BranchName(value: String) extends AnyVal
  final case class TagName(value: String) extends AnyVal
  final case class Repository(value: String) extends AnyVal
  final case class RemoteName(remoteName: String) extends AnyVal
  final case class RepoUrl(repoUrl: String) extends AnyVal
  final case class Description(value: String) extends AnyVal

  def fromProcessResultToEither[A](
    gitCmd: GitCmd
  , successHandler: List[String] => A
  , errorHandler: (GitCmd, Int, List[String]) => GitCommandError
  ): PartialFunction[ProcessResult, Either[GitCommandError, (GitCommandResult, A)]] = {
      case ProcessResult.Success(outputs) =>
        (GitCommandResult.genericResult(outputs), successHandler(outputs)).right

      case ProcessResult.Failure(code, errors) =>
        errorHandler(gitCmd, code, errors).left
    }

  def git(baseDir: File, commandAndArgs: List[String]): ProcessResult =
    SysProcess.run(
      SysProcess.process(Some(baseDir), "git" :: commandAndArgs)
    )

  def git1(baseDir: File, command: String, args: String*): ProcessResult =
    git(baseDir, command :: args.toList)

  def gitCmd[A](
    baseDir: File
  , gitCmd: GitCmd
  , f: List[String] => A
  , e: (GitCmd, Int, List[String]) => GitCommandError
  ): Either[GitCommandError, (GitCommandResult, A)] = {
    val gitCmdAndArgs = GitCmd.cmdAndArgs(gitCmd)
    ProcessResult.toEither(
      git(baseDir, gitCmdAndArgs)
    )(
      fromProcessResultToEither(gitCmd, f, e)
    )
  }

  def gitCmdSimple[A](
      baseDir: File
    , cmd: GitCmd
    , resultHandler: List[String] => A
    ): Either[GitCommandError, (GitCommandResult, A)] =
    gitCmd(
        baseDir
      , cmd
      , resultHandler
      , GitCommandError.genericGotCommandResultError
    )

  def gitCmdSimpleWithWriter[A](
      baseDir: File
    , cmd: GitCmd
    , resultHandler: List[String] => A
    ): CmdHistoryWriter[Either[GitCommandError, A]] = {
    updateHistory(
      cmd
    , gitCmdSimple(
        baseDir
      , cmd
      , resultHandler
      )
    )
  }

  def updateHistory[A](
    gitCmd: GitCmd
  , r: Either[GitCommandError, (GitCommandResult, A)]
  ): CmdHistoryWriter[Either[GitCommandError, A]] = r match {
    case Left(error) =>
      Writer(List.empty, error.left)
    case Right((cmdResult, a)) =>
      Writer(List(GitCmdAndResult(gitCmd, cmdResult)), a.right)
  }

  def currentBranchName(baseDir: File): CmdResult[BranchName] = EitherT(
    gitCmdSimpleWithWriter[BranchName](
      baseDir
    , GitCmd.currentBranchName
    , xs => BranchName(xs.mkString.trim)
    )
  )

  def checkIfCurrentBranchIsSame(
    branchName: BranchName
  , baseDir: File
  ): CmdResult[Boolean] = for {
    current <- currentBranchName(baseDir)
  } yield current.value === branchName.value


  def checkout(branchName: BranchName, baseDir: File): CmdResult[Unit] = EitherT {
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.checkout(branchName)
    , _ => ()
    )
}

  def fetchTags(baseDir: File): CmdResult[List[String]] = EitherT(
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.fetchTags
    , identity
    )
  )

  def tag(tagName: TagName, baseDir: File): CmdResult[TagName] = EitherT(
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.tag(tagName)
    , _ => tagName
    )
  )

  def tagWithDescription(
    tagName: TagName
  , description: Description
  , baseDir: File
  ): CmdResult[TagName] = EitherT(
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.tagWithDescription(tagName, description)
    , _ => tagName
    )
  )

  def pushTag(
    repository: Repository
  , tagName: TagName
  , baseDir:File
  ): CmdResult[List[String]] = EitherT(
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.push(repository, tagName)
    , identity
    )
  )

  def getRemoteUrl(repository: Repository, baseDir:File): CmdResult[RepoUrl] = EitherT(
    gitCmdSimpleWithWriter(
      baseDir
    , GitCmd.remoteGetUrl(repository)
    , xs => RepoUrl(xs.mkString.trim)
    )
  )

}