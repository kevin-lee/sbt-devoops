package kevinlee.git

import kevinlee.git.Git.{BranchName, Description, Repository, TagName}

/**
  * @author Kevin Lee
  * @since 2019-03-17
  */
final case class SuccessHistory(history: List[(GitCmd, GitCommandResult)])
object SuccessHistory {
  val empty: SuccessHistory = SuccessHistory(Nil)

  def render(successHistory: SuccessHistory): String = {
    val delimiter = ">> "
    successHistory.history.reverse
      .map{ case (cmd, result) =>
        s"${GitCmd.render(cmd)} => ${GitCommandResult.render(result)}"
      }.mkString(delimiter, s"\n$delimiter", "")
  }
}

final case class GitCmdMonad[A](run: SuccessHistory => (SuccessHistory, Either[GitCommandError, A])) {
  def map[B](f: A => B): GitCmdMonad[B] = GitCmdMonad { history =>
    val (next, r) = run(history)
    r match {
      case Left(_) =>
        @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
        val sameR = r.asInstanceOf[Either[GitCommandError, B]]
        (next, sameR)
      case Right(a) =>
        (next, Right(f(a)))
    }
  }

  def flatMap[B](f: A => GitCmdMonad[B]): GitCmdMonad[B] = GitCmdMonad { history =>
    val (next, r) = run(history)
    r match {
      case Left(_) =>
        @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
        val sameR = r.asInstanceOf[Either[GitCommandError, B]]
        (next, sameR)
      case Right(a) =>
        f(a).run(next)
    }
  }
}

object GitCmdMonad {
  def pureLeft[A, B](a: GitCommandError): GitCmdMonad[B] = GitCmdMonad(s => (s, Left(a)))
  def pure[A, B](b: B): GitCmdMonad[B] = GitCmdMonad(s => (s, Right(b)))
}

sealed trait GitCmd

object GitCmd {
  final case object CurrentBranchName extends GitCmd
  final case class Checkout(branchName: BranchName) extends GitCmd
  final case object FetchTags extends GitCmd
  final case class Tag(tagName: TagName) extends GitCmd
  final case class TagWithDescription(tagName: TagName, description: Description) extends GitCmd
  final case class Push(repository: Repository, tagName: TagName) extends GitCmd
  final case class RemoteGetUrl(repository: Repository) extends GitCmd

  def currentBranchName: GitCmd = CurrentBranchName

  def checkout(branchName: BranchName): GitCmd = Checkout(branchName)

  def fetchTags: GitCmd = FetchTags

  def tag(tagName: TagName): GitCmd = Tag(tagName)

  def tagWithDescription(tagName: TagName, description: Description): GitCmd =
    TagWithDescription(tagName, description)

  def push(repository: Repository, tagName: TagName): GitCmd =
    Push(repository, tagName)

  def remoteGetUrl(repository: Repository): GitCmd =
    RemoteGetUrl(repository)

  def cmdAndArgs(gitCmd: GitCmd): List[String] = gitCmd match {
    case CurrentBranchName =>
      List("rev-parse", "--abbrev-ref", "HEAD")

    case Checkout(branchName) =>
      List("checkout", branchName.value)

    case FetchTags =>
      List("fetch", "--tags")

    case Tag(tagName) =>
      List("tag", tagName.value)

    case TagWithDescription(tagName, description) =>
      List("tag", "-a", tagName.value, "-m", description.value)

    case Push(repository, tagName) =>
      List("push", repository.value, tagName.value)

    case RemoteGetUrl(repository) =>
      List("remote", "get-url", repository.value)
  }

  def render(gitCmd: GitCmd): String = s"git ${cmdAndArgs(gitCmd).mkString(" ")}"

}
