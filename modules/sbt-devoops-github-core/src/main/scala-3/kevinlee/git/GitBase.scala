package kevinlee.git

import refined4s.*
import refined4s.modules.circe.derivation.CirceNewtypeCodec

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
  type BranchName = BranchName.Type
  object BranchName extends Newtype[String]

  type TagName = TagName.Type
  object TagName extends Newtype[String], CirceNewtypeCodec[String]

  type Repository = Repository.Type
  object Repository extends Newtype[String]

  type RemoteName = RemoteName.Type
  object RemoteName extends Newtype[String] {
    extension (a: RemoteName) def remoteName: String = a.value
  }

  type RepoUrl = RepoUrl.Type
  object RepoUrl extends Newtype[String] {
    extension (a: RepoUrl) def repoUrl: String = a.value
  }

  type Description = Description.Type
  object Description extends Newtype[String]

  type TagMessage = TagMessage.Type
  object TagMessage extends Newtype[String] {
    extension (a: TagMessage) def tagMessage: String = a.value
  }

  type HashObject = HashObject.Type
  object HashObject extends Newtype[String] {
    extension (a: HashObject) def hashObject: String = a.value
  }
}
