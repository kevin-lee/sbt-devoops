package kevinlee.git

import cats.*
import cats.data.*
import cats.syntax.all.*
import effectie.syntax.all.*
import effectie.core.*

import java.io.File

/** Algebra for running `git` commands as external processes.
  *
  * The methods are layered, from the lowest level to the highest.
  *   1. [[git]] / [[git1]] run `git` synchronously and return the raw [[ProcessResult]]. They are not wrapped in `F`.
  *   1. [[fromProcessResultToEither]] converts a [[ProcessResult]] into an `Either`.
  *   1. [[gitCmd]] / [[gitCmdSimple]] turn a [[GitCmd]] into its arguments, run it inside `F`, and convert the result
  *      into `F[Either[GitCommandError, (GitCommandResult, A)]]`.
  *   1. [[updateHistory]] / [[gitCmdSimpleWithWriter]] lift that into [[Git.CmdResult]], which also records every
  *      successful command and its output in a [[Git.CmdHistory]] log (via `WriterT`).
  *   1. The domain operations ([[currentBranchName]], [[checkout]], [[tag]], [[pushTag]], etc.) are thin wrappers
  *      around [[gitCmdSimpleWithWriter]] that only choose the [[GitCmd]] and decide how to turn the output lines into
  *      a meaningful value.
  *
  * The resulting [[Git.CmdResult]] values compose in a for-comprehension. The first failed command short-circuits
  * the rest (`EitherT`), and the history of all the successful commands so far is accumulated (`WriterT`), so the
  * caller can log what was done.
  *
  * @author Kevin Lee
  * @since 2019-01-01
  */
trait Git[F[_]] {
  import Git.*

  /** Builds a function that converts a [[ProcessResult]] into an `Either`.
    *
    *   - [[ProcessResult.Success]] becomes `Right((GitCommandResult.GenericResult(outputs), successHandler(outputs)))`.
    *     The raw output is kept in the [[GitCommandResult]] for the history log, and `successHandler` parses the same
    *     output into the value the caller actually wants.
    *   - [[ProcessResult.Failure]] becomes `Left(errorHandler(gitCmd, exitCode, errors))`.
    *
    * @param gitCmd the command that was run. It is only used to build the error.
    * @param successHandler parses the output lines (stdout followed by stderr, see [[ProcessResult.processResult]])
    * @param errorHandler builds the error from the command, exit code and stderr lines
    */
  def fromProcessResultToEither[A](
    gitCmd: GitCmd,
    successHandler: List[String] => A,
    errorHandler: (GitCmd, Int, List[String]) => GitCommandError,
  ): ProcessResult => Either[GitCommandError, (GitCommandResult, A)]

  /** Runs `git` with the given command and arguments in `baseDir`, and blocks until it finishes.
    *
    * This is side-effecting and NOT suspended in `F`. It runs as soon as it is called. Use [[gitCmd]] or one of the
    * higher level methods to get a suspended effect.
    *
    * @param commandAndArgs e.g. `List("tag", "-a", "v1.0.0", "-m", "Release v1.0.0")`. `"git"` is prepended.
    */
  def git(baseDir: File, commandAndArgs: List[String]): ProcessResult

  /** Varargs version of [[git]]. `git1(dir, "tag", "v1.0.0")` is the same as `git(dir, List("tag", "v1.0.0"))`. */
  def git1(baseDir: File, command: String, args: String*): ProcessResult

  /** Runs the given [[GitCmd]] inside `F` with a custom success handler (`f`) and error handler (`e`).
    *
    * The [[GitCmd]] is rendered into `git` arguments by [[GitCmd.cmdAndArgs]], and the process is run inside
    * `effectOf`, so nothing happens until `F` is run.
    *
    * @param f parses the output lines into `A` on success
    * @param e builds a [[GitCommandError]] from the command, exit code and stderr lines on failure
    * @return the raw result (for the history) paired with the parsed value, or the error
    */
  def gitCmd[A](
    baseDir: File,
    gitCmd: GitCmd,
    f: List[String] => A,
    e: (GitCmd, Int, List[String]) => GitCommandError,
  ): F[Either[GitCommandError, (GitCommandResult, A)]]

  /** Same as [[gitCmd]] but uses [[GitCommandError.genericGitCommandResultError]] as the error handler. */
  def gitCmdSimple[A](
    baseDir: File,
    cmd: GitCmd,
    resultHandler: List[String] => A,
  ): F[Either[GitCommandError, (GitCommandResult, A)]]

  /** [[gitCmdSimple]] plus [[updateHistory]]. This is the building block for all the domain operations below.
    * It runs the command and records it in the [[Git.CmdHistory]] if it succeeds.
    */
  def gitCmdSimpleWithWriter[A](
    baseDir: File,
    cmd: GitCmd,
    resultHandler: List[String] => A,
  ): CmdResult[F, A]

  /** Lifts the result of running `gitCmd` into [[Git.CmdResult]].
    *
    *   - On success, the history gets one entry, `GitCmdAndResult(gitCmd, cmdResult)`, and the value is `Right(a)`.
    *   - On failure, the history gets nothing and the value is `Left(error)`. The failed command is not recorded
    *     in the history. It is only described by the [[GitCommandError]] itself.
    *
    * The [[GitCommandResult]] is moved into the history, so only `A` remains in the result.
    */
  def updateHistory[A](
    gitCmd: GitCmd,
    r: F[Either[GitCommandError, (GitCommandResult, A)]],
  ): CmdResult[F, A]

  /** `git rev-parse --abbrev-ref HEAD`. Returns the current branch name (or `HEAD` when detached). */
  def currentBranchName(baseDir: File): CmdResult[F, BranchName]

  /** Checks whether the current branch is `branchName`, using [[currentBranchName]]. */
  def checkIfCurrentBranchIsSame(
    branchName: BranchName,
    baseDir: File,
  ): CmdResult[F, Boolean]

  /** `git checkout <branchName>`. The output is discarded. */
  def checkout(branchName: BranchName, baseDir: File): CmdResult[F, Unit]

  /** `git fetch --tags`. Returns the raw output lines. */
  def fetchTags(baseDir: File): CmdResult[F, List[String]]

  /** `git tag`. Returns all the tag names, one per line. */
  def getTag(baseDir: File): CmdResult[F, List[String]]

  /** `git tag <tagName>` (lightweight tag). Returns the given `tagName` on success. */
  def tag(tagName: TagName, baseDir: File): CmdResult[F, TagName]

  /** `git tag -a <tagName> -m <description>` (annotated tag). Returns the given `tagName` on success. */
  def tagWithDescription(
    tagName: TagName,
    description: Description,
    baseDir: File,
  ): CmdResult[F, TagName]

  /** `git push <repository> <tagName>`. Returns the raw output lines.
    *
    * @param repository the remote to push to (e.g. `origin`)
    */
  def pushTag(
    repository: Repository,
    tagName: TagName,
    baseDir: File,
  ): CmdResult[F, List[String]]

  /** `git remote get-url <repository>`. Returns the remote URL. */
  def getRemoteUrl(repository: Repository, baseDir: File): CmdResult[F, RepoUrl]

}

/** Also brings the newtypes such as `BranchName`, `TagName`, `Repository` and `RepoUrl` in from [[GitBase]]. */
object Git extends GitBase {
  // $COVERAGE-OFF$

  /** The successful commands run so far, in the order they were run, with their outputs. */
  type CmdHistory = List[GitCmdAndResult]

  /** `F[(CmdHistory, A)]`. A computation in `F` that also accumulates [[CmdHistory]]. */
  type CmdHistoryWriter[F[_], A] = WriterT[F, CmdHistory, A]

  /** The result type of every git operation. Its underlying structure is `F[(CmdHistory, Either[GitCommandError, A])]`.
    *
    *   - `EitherT` gives short-circuiting on the first [[GitCommandError]] in a for-comprehension.
    *   - `WriterT` accumulates the [[CmdHistory]] of every successful step.
    *
    * `EitherT` is the outer layer, so the history is kept even when a later step fails. Use `.value.run` to get
    * `F[(CmdHistory, Either[GitCommandError, A])]`.
    */
  type CmdResult[F[_], A] = EitherT[CmdHistoryWriter[F, *], GitCommandError, A]

  /** Summoner. `Git[F]` is the same as `implicitly[Git[F]]`. */
  def apply[F[_]: Git]: Git[F] = implicitly[Git[F]]

  /** The default instance for any `F` that has effectie's `Fx` (to suspend the process call) and `Monad`. */
  implicit def gitF[F[_]: Fx: Monad]: Git[F] = new GitF[F]

  final class GitF[F[_]: Fx: Monad] extends Git[F] {

    override def fromProcessResultToEither[A](
      gitCmd: GitCmd,
      successHandler: List[String] => A,
      errorHandler: (GitCmd, Int, List[String]) => GitCommandError,
    ): ProcessResult => Either[GitCommandError, (GitCommandResult, A)] = {
      case ProcessResult.Success(outputs) =>
        /* Keep the raw output as GitCommandResult for the history, and also the parsed value. */
        (GitCommandResult.genericResult(outputs), successHandler(outputs)).asRight

      case ProcessResult.Failure(code, errors) =>
        errorHandler(gitCmd, code, errors).asLeft
    }

    override def git(baseDir: File, commandAndArgs: List[String]): ProcessResult =
      /* Runs `git ...` in baseDir, blocks until it exits, and collects stdout and stderr lines. */
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
      /* Render the GitCmd ADT into the actual git arguments (e.g. CurrentBranchName => rev-parse --abbrev-ref HEAD). */
      gitCmdAndArgs <- pureOf(GitCmd.cmdAndArgs(gitCmd))
      /* The process call is side-effecting, so it must be suspended with effectOf.
       * Its ProcessResult is then converted into Either by fromProcessResultToEither.
       */
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
        GitCommandError.genericGitCommandResultError,
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
      /* Reshapes F[Either[E, (GitCommandResult, A)]]
       * into F[(CmdHistory, Either[E, A])], which is what WriterT and then EitherT wrap.
       */
      EitherT {
        val fOf = r.map { eth =>
          /* The history entry for this command. Only a successful command is recorded. */
          val w: CmdHistory =
            eth match {
              case Left(error @ _) =>
                List.empty[GitCmdAndResult]
              case Right((cmdResult, a @ _)) =>
                List(GitCmdAndResult(gitCmd, cmdResult))
            }

          /* GitCommandResult has already gone to the history, so drop it and keep only A. */
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
        /* The output is a single line with the branch name. trim removes any surrounding whitespace. */
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

    override def getTag(baseDir: File): CmdResult[F, List[String]] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.getTag,
        identity,
      )

    override def tag(tagName: TagName, baseDir: File): CmdResult[F, TagName] =
      gitCmdSimpleWithWriter(
        baseDir,
        GitCmd.tag(tagName),
        /* git tag prints nothing on success, so return the tag name that was created. */
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
        /* git push writes its progress to stderr. On success, stderr is appended to the outputs
         * (see ProcessResult.processResult), so the push progress is included here.
         */
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
