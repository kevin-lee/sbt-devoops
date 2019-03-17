package kevinlee.git

import java.io.File

import kevinlee.CommonPredef._

/**
  * @author Kevin Lee
  * @since 2019-01-01
  */
object Git {
  // $COVERAGE-OFF$

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

  def updateHistory[A](
    history: SuccessHistory
  , gitCmd: GitCmd
  , r: Either[GitCommandError, (GitCommandResult, A)]
  ): (SuccessHistory, Either[GitCommandError, A]) = r match {
    case Left(error) =>
      (history, Left(error))
    case Right((cmdResult, a)) =>
      (SuccessHistory((gitCmd, cmdResult) :: history.history), Right(a))
  }

  def currentBranchName(baseDir: File): GitCmdMonad[BranchName] = GitCmdMonad { history =>
    val cmd = GitCmd.currentBranchName
    updateHistory(
        history
      , cmd
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
  ): GitCmdMonad[Boolean] = for {
    current <- currentBranchName(baseDir)
  } yield current.value === branchName.value


  def checkout(branchName: BranchName, baseDir: File): GitCmdMonad[Unit] = GitCmdMonad { history =>
    val cmd = GitCmd.checkout(branchName)
    updateHistory(
        history
      , cmd
      , gitCmd(
          baseDir
        , cmd
        , _ => ()
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def fetchTags(baseDir: File): GitCmdMonad[List[String]] = GitCmdMonad { history =>
    val cmd = GitCmd.fetchTags
    updateHistory(
      history
    , cmd
    , gitCmd(
          baseDir
        , cmd
        , identity
        , GitCommandError.genericGotCommandResultError
      )
    )

  }

  def tag(tagName: TagName, baseDir: File): GitCmdMonad[TagName] = GitCmdMonad { history =>
    val cmd = GitCmd.tag(tagName)
    updateHistory(
        history
      , cmd
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
  ): GitCmdMonad[TagName] = GitCmdMonad { history =>
    val cmd = GitCmd.tagWithDescription(tagName, description)
    updateHistory(
        history
      , cmd
      , gitCmd(
          baseDir
        , cmd
        , _ => tagName
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

  def pushTag(repository: Repository, tagName: TagName, baseDir:File): GitCmdMonad[List[String]] =
    GitCmdMonad { history =>
      val cmd = GitCmd.push(repository, tagName)
      updateHistory(
          history
        , cmd
        , gitCmd(
            baseDir
          , cmd
          , identity
          , GitCommandError.genericGotCommandResultError
        )
      )
    }

  def getRemoteUrl(repository: Repository, baseDir:File): GitCmdMonad[RepoUrl] = GitCmdMonad { history =>
    val cmd = GitCmd.remoteGetUrl(repository)
    updateHistory(
        history
      , cmd
      , gitCmd(
          baseDir
        , cmd
        , xs => RepoUrl(xs.mkString.trim)
        , GitCommandError.genericGotCommandResultError
      )
    )
  }

}