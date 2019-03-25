package kevinlee.git

import java.io.File

import kevinlee.CommonPredef._
import kevinlee.fp.Writer.Writer
import kevinlee.fp.{EitherT, Writer}

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object Git {
  // $COVERAGE-OFF$

  type GitCmdHistory = List[(GitCmd, GitCommandResult)]

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
        Right((GitCommandResult.genericResult(outputs), successHandler(outputs)))

      case ProcessResult.Failure(code, errors) =>
        Left(errorHandler(gitCmd, code, errors))
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

  type GitCmdHistoryWriter[A] = Writer[GitCmdHistory, A]

  def updateHistory[A](
    gitCmd: GitCmd
  , r: Either[GitCommandError, (GitCommandResult, A)]
  ): GitCmdHistoryWriter[Either[GitCommandError, A]] = r match {
    case Left(error) =>
      Writer(List.empty, Left(error))
    case Right((cmdResult, a)) =>
      Writer(List((gitCmd, cmdResult)), Right(a))
  }

  def currentBranchName(baseDir: File): EitherT[GitCmdHistoryWriter, GitCommandError, BranchName] = EitherT {
    val cmd = GitCmd.currentBranchName
    updateHistory(
        cmd
      , gitCmd[BranchName](
          baseDir
        , cmd
        , xs => BranchName(xs.mkString.trim)
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def checkIfCurrentBranchIsSame(
    branchName: BranchName
  , baseDir: File
  ): EitherT[GitCmdHistoryWriter, GitCommandError, Boolean] = for {
    current <- currentBranchName(baseDir)
  } yield current.value === branchName.value


  def checkout(branchName: BranchName, baseDir: File): EitherT[GitCmdHistoryWriter, GitCommandError, Unit] = EitherT {
    val cmd = GitCmd.checkout(branchName)
    updateHistory(
        cmd
      , gitCmd(
          baseDir
        , cmd
        , _ => ()
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def fetchTags(baseDir: File): EitherT[GitCmdHistoryWriter, GitCommandError, List[String]] = EitherT {
    val cmd = GitCmd.fetchTags
    updateHistory(
      cmd
    , gitCmd(
          baseDir
        , cmd
        , identity
        , GitCommandError.genericGotCommandResultError
      )
    )

  }

  def tag(tagName: TagName, baseDir: File): EitherT[GitCmdHistoryWriter, GitCommandError, TagName] = EitherT {
    val cmd = GitCmd.tag(tagName)
    updateHistory(
        cmd
      , gitCmd(
          baseDir
        , cmd
        , _ => tagName
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def tagWithDescription(
    tagName: TagName
  , description: Description
  , baseDir: File
  ): EitherT[GitCmdHistoryWriter, GitCommandError, TagName] = EitherT {
    val cmd = GitCmd.tagWithDescription(tagName, description)
    updateHistory(
        cmd
      , gitCmd(
          baseDir
        , cmd
        , _ => tagName
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def pushTag(repository: Repository, tagName: TagName, baseDir:File): EitherT[GitCmdHistoryWriter, GitCommandError, List[String]] =
    EitherT {
      val cmd = GitCmd.push(repository, tagName)
      updateHistory(
          cmd
        , gitCmd(
            baseDir
          , cmd
          , identity
          , GitCommandError.genericGotCommandResultError
        )
      )
    }

  def getRemoteUrl(repository: Repository, baseDir:File): EitherT[GitCmdHistoryWriter, GitCommandError, RepoUrl] = EitherT {
    val cmd = GitCmd.remoteGetUrl(repository)
    updateHistory(
        cmd
      , gitCmd(
          baseDir
        , cmd
        , xs => RepoUrl(xs.mkString.trim)
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

}