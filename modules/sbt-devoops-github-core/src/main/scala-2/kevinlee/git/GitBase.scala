package kevinlee.git

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

/** @author Kevin Lee
  * @since 2019-01-01
  */
trait GitBase {
  type BranchName = GitBase.BranchName
  val BranchName = GitBase.BranchName

  type TagName = GitBase.TagName
  val TagName = GitBase.TagName

  type Repository = GitBase.Repository
  val Repository = GitBase.Repository

  type RemoteName = GitBase.RemoteName
  val RemoteName = GitBase.RemoteName

  type RepoUrl = GitBase.RepoUrl
  val RepoUrl = GitBase.RepoUrl

  type Description = GitBase.Description
  val Description = GitBase.Description

  type TagMessage = GitBase.TagMessage
  val TagMessage = GitBase.TagMessage

  type HashObject = GitBase.HashObject
  val HashObject = GitBase.HashObject
}
object GitBase {
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
}
