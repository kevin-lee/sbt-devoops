package kevinlee.github.data

import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import refined4s.*
import refined4s.modules.circe.derivation.CirceNewtypeCodec
import kevinlee.git.Git

import java.io.File
import java.time.Instant

/** @author Kevin Lee
  * @since 2021-01-16
  */
trait GitHubReleaseBase {
  type CreateRequestParams = GitHubReleaseBase.CreateRequestParams
  val CreateRequestParams = GitHubReleaseBase.CreateRequestParams

  type UpdateRequestParams = GitHubReleaseBase.UpdateRequestParams
  val UpdateRequestParams = GitHubReleaseBase.UpdateRequestParams

  type GenerateNotesRequestParams = GitHubReleaseBase.GenerateNotesRequestParams
  val GenerateNotesRequestParams = GitHubReleaseBase.GenerateNotesRequestParams

  type UploadAssetParams = GitHubReleaseBase.UploadAssetParams
  val UploadAssetParams = GitHubReleaseBase.UploadAssetParams

  type Accept = GitHubReleaseBase.Accept
  val Accept = GitHubReleaseBase.Accept

  type ReleaseId = GitHubReleaseBase.ReleaseId
  val ReleaseId = GitHubReleaseBase.ReleaseId

  type ReleaseName = GitHubReleaseBase.ReleaseName
  val ReleaseName = GitHubReleaseBase.ReleaseName

  type Description = GitHubReleaseBase.Description
  val Description = GitHubReleaseBase.Description

  type Draft = GitHubReleaseBase.Draft
  val Draft = GitHubReleaseBase.Draft

  type Prerelease = GitHubReleaseBase.Prerelease
  val Prerelease = GitHubReleaseBase.Prerelease

  type GenerateReleaseNotes = GitHubReleaseBase.GenerateReleaseNotes
  val GenerateReleaseNotes = GitHubReleaseBase.GenerateReleaseNotes

  type GeneratedNotes = GitHubReleaseBase.GeneratedNotes
  val GeneratedNotes = GitHubReleaseBase.GeneratedNotes

  type Asset = GitHubReleaseBase.Asset
  val Asset = GitHubReleaseBase.Asset

  type Response = GitHubReleaseBase.Response
  val Response = GitHubReleaseBase.Response
}
object GitHubReleaseBase {
  final case class CreateRequestParams(
    tagName: Git.TagName,
    name: Option[ReleaseName],
    body: Option[Description],
    draft: Draft,
    prerelease: Prerelease,
    generateReleaseNotes: GenerateReleaseNotes,
  )
  object CreateRequestParams {
    given encoder: Encoder[CreateRequestParams] =
      requestParams =>
        Json.obj(
          (List("tag_name" -> requestParams.tagName.asJson) ++
            requestParams.name.toList.map(name => "name" -> name.asJson) ++
            requestParams.body.toList.map(body => "body" -> body.asJson) ++
            List(
              "draft"                  -> requestParams.draft.asJson,
              "prerelease"             -> requestParams.prerelease.asJson,
              "generate_release_notes" -> requestParams.generateReleaseNotes.asJson,
            ))*
        )

    given decoder: Decoder[CreateRequestParams] =
      c =>
        for {
          tagName              <- c.downField("tag_name").as[Git.TagName]
          name                 <- c.downField("name").as[Option[ReleaseName]]
          body                 <- c.downField("body").as[Option[Description]]
          draft                <- c.downField("draft").as[Draft]
          prerelease           <- c.downField("prerelease").as[Prerelease]
          generateReleaseNotes <- c.downField("generate_release_notes").as[GenerateReleaseNotes]
        } yield CreateRequestParams(tagName, name, body, draft, prerelease, generateReleaseNotes)

  }

  final case class UpdateRequestParams(
    tagName: Git.TagName,
    releaseId: ReleaseId,
    name: Option[ReleaseName],
    body: Option[Description],
    draft: Option[Draft],
    prerelease: Option[Prerelease],
  )
  object UpdateRequestParams {
    given encoder: Encoder[UpdateRequestParams] =
      requestParams =>
        Json.obj(
          (
            List(
              "tag_name"   -> requestParams.tagName.asJson,
              "release_id" -> requestParams.releaseId.asJson,
            ) ++
              requestParams.name.toList.map(name => "name" -> name.asJson) ++
              requestParams.body.toList.map(body => "body" -> body.asJson) ++
              requestParams.draft.toList.map(draft => "draft" -> draft.asJson) ++
              requestParams.prerelease.toList.map(prerelease => "prerelease" -> prerelease.asJson)
          )*
        )

    given decoder: Decoder[UpdateRequestParams] =
      c =>
        for {
          tagName    <- c.downField("tag_name").as[Git.TagName]
          releaseId  <- c.downField("release_id").as[ReleaseId]
          name       <- c.downField("name").as[Option[ReleaseName]]
          body       <- c.downField("body").as[Option[Description]]
          draft      <- c.downField("draft").as[Option[Draft]]
          prerelease <- c.downField("prerelease").as[Option[Prerelease]]
        } yield UpdateRequestParams(tagName, releaseId, name, body, draft, prerelease)

  }

  final case class GenerateNotesRequestParams(
    tagName: Git.TagName
  )
  object GenerateNotesRequestParams {
    given encoder: Encoder[GenerateNotesRequestParams] =
      requestParams =>
        Json.obj(
          "tag_name" -> requestParams.tagName.asJson
        )

    given decoder: Decoder[GenerateNotesRequestParams] =
      _.downField("tag_name").as[Git.TagName].map(GenerateNotesRequestParams(_))

  }

  final case class UploadAssetParams(
    releaseId: ReleaseId,
    name: UploadAssetParams.AssetName,
    label: Option[UploadAssetParams.AssetLabel],
    assetFile: UploadAssetParams.AssetFile,
  )
  object UploadAssetParams {
    type AssetName = AssetName.Type
    object AssetName extends Newtype[String] {
      extension (a: AssetName) def assetName: String = a.value
    }

    type AssetLabel = AssetLabel.Type
    object AssetLabel extends Newtype[String] {
      extension (a: AssetLabel) def assetLabel: String = a.value
    }

    final case class AssetFile(assetFile: File, contentTypes: List[GitHubRelease.Asset.ContentType])

  }

  type Accept = Accept.Type
  object Accept extends Newtype[String], CirceNewtypeCodec[String] {
    extension (a: Accept) def accept: String = a.value
  }

  type ReleaseId = ReleaseId.Type
  object ReleaseId extends Newtype[Long], CirceNewtypeCodec[Long] {
    extension (a: ReleaseId) def releaseId: Long = a.value
  }

  type ReleaseName = ReleaseName.Type
  object ReleaseName extends Newtype[String], CirceNewtypeCodec[String] {
    extension (a: ReleaseName) def releaseName: String = a.value
  }

  type Description = Description.Type
  object Description extends Newtype[String], CirceNewtypeCodec[String] {
    extension (a: Description) def description: String = a.value
  }

  sealed trait Draft
  object Draft {
    case object Yes extends Draft
    case object No extends Draft

    def yes: Draft = Yes
    def no: Draft  = No

    def toBoolean(draft: Draft): Boolean = draft match {
      case Draft.Yes =>
        true
      case Draft.No =>
        false
    }

    def fromBoolean(draft: Boolean): Draft =
      if (draft)
        Draft.yes
      else
        Draft.no

    implicit val encoder: Encoder[Draft] = draft => Json.fromBoolean(Draft.toBoolean(draft))
    implicit val decoder: Decoder[Draft] = _.as[Boolean].map(Draft.fromBoolean)

  }

  sealed trait Prerelease
  object Prerelease {
    case object Yes extends Prerelease
    case object No extends Prerelease

    def yes: Prerelease = Yes
    def no: Prerelease  = No

    def toBoolean(prerelease: Prerelease): Boolean = prerelease match {
      case Prerelease.Yes =>
        true
      case Prerelease.No =>
        false
    }

    def fromBoolean(prerelease: Boolean): Prerelease =
      if (prerelease)
        Prerelease.yes
      else
        Prerelease.no

    implicit val encoder: Encoder[Prerelease] = a => Json.fromBoolean(Prerelease.toBoolean(a))

    implicit val decoder: Decoder[Prerelease] = _.as[Boolean].map(Prerelease.fromBoolean)

  }

  sealed trait GenerateReleaseNotes
  object GenerateReleaseNotes {
    case object Yes extends GenerateReleaseNotes
    case object No extends GenerateReleaseNotes

    def yes: GenerateReleaseNotes = Yes
    def no: GenerateReleaseNotes  = No

    def toBoolean(generateReleaseNotes: GenerateReleaseNotes): Boolean = generateReleaseNotes match {
      case GenerateReleaseNotes.Yes =>
        true
      case GenerateReleaseNotes.No =>
        false
    }

    def fromBoolean(generateReleaseNotes: Boolean): GenerateReleaseNotes =
      if (generateReleaseNotes)
        GenerateReleaseNotes.yes
      else
        GenerateReleaseNotes.no

    implicit val encoder: Encoder[GenerateReleaseNotes] =
      a => Json.fromBoolean(GenerateReleaseNotes.toBoolean(a))

    implicit val decoder: Decoder[GenerateReleaseNotes] = _.as[Boolean].map(GenerateReleaseNotes.fromBoolean)

  }

  final case class GeneratedNotes(
    name: ReleaseName,
    body: Description,
  )
  object GeneratedNotes {
    given encoder: Encoder[GeneratedNotes] =
      generatedNotes =>
        Json.obj(
          "name" -> generatedNotes.name.asJson,
          "body" -> generatedNotes.body.asJson,
        )

    given decoder: Decoder[GeneratedNotes] =
      c =>
        for {
          name <- c.downField("name").as[ReleaseName]
          body <- c.downField("body").as[Description]
        } yield GeneratedNotes(name, body)

  }

  final case class Asset(
    id: Asset.Id,
    url: Asset.Url,
    browserDownloadUrl: Asset.BrowserDownloadUrl,
    name: Asset.Name,
    label: Asset.Label,
    state: Asset.State,
    contentType: Asset.ContentType,
    size: Asset.Size,
    downloadCount: Asset.DownloadCount,
    createdAt: Asset.CreatedAt,
    updatedAt: Asset.UpdatedAt,
    uploader: GitHub.User,
  )
  object Asset {
    type Id = Id.Type
    object Id extends Newtype[Long], CirceNewtypeCodec[Long] {
      extension (a: Id) def id: Long = a.value
    }

    type Url = Url.Type
    object Url extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: Url) def url: String = a.value
    }

    type BrowserDownloadUrl = BrowserDownloadUrl.Type
    object BrowserDownloadUrl extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: BrowserDownloadUrl) def browserDownloadUrl: String = a.value
    }

    type Name = Name.Type
    object Name extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: Name) def name: String = a.value
    }

    type Label = Label.Type
    object Label extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: Label) def label: String = a.value
    }

    type State = State.Type
    object State extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: State) def state: String = a.value
    }

    type ContentType = ContentType.Type
    object ContentType extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: ContentType) def contentType: String = a.value
    }

    type Size = Size.Type
    object Size extends Newtype[Long], CirceNewtypeCodec[Long] {
      extension (a: Size) def size: Long = a.value
    }

    type DownloadCount = DownloadCount.Type
    object DownloadCount extends Newtype[Int], CirceNewtypeCodec[Int] {
      extension (a: DownloadCount) def downloadCount: Int = a.value
    }

    type CreatedAt = CreatedAt.Type
    object CreatedAt extends Newtype[Instant], CirceNewtypeCodec[Instant] {
      extension (a: CreatedAt) def createdAt: Instant = a.value
    }

    type UpdatedAt = UpdatedAt.Type
    object UpdatedAt extends Newtype[Instant], CirceNewtypeCodec[Instant] {
      extension (a: UpdatedAt) def updatedAt: Instant = a.value
    }

    given encoder: Encoder[Asset] =
      asset =>
        Json.obj(
          "id"                   -> asset.id.asJson,
          "url"                  -> asset.url.asJson,
          "browser_download_url" -> asset.browserDownloadUrl.asJson,
          "name"                 -> asset.name.asJson,
          "label"                -> asset.label.asJson,
          "state"                -> asset.state.asJson,
          "content_type"         -> asset.contentType.asJson,
          "size"                 -> asset.size.asJson,
          "download_count"       -> asset.downloadCount.asJson,
          "created_at"           -> asset.createdAt.asJson,
          "updated_at"           -> asset.updatedAt.asJson,
          "uploader"             -> asset.uploader.asJson,
        )
    given decoder: Decoder[Asset] =
      c =>
        for {
          id                 <- c.downField("id").as[Id]
          url                <- c.downField("url").as[Url]
          browserDownloadUrl <- c.downField("browser_download_url").as[BrowserDownloadUrl]
          name               <- c.downField("name").as[Name]
          label              <- c.downField("label").as[Label]
          state              <- c.downField("state").as[State]
          contentType        <- c.downField("content_type").as[ContentType]
          size               <- c.downField("size").as[Size]
          downloadCount      <- c.downField("download_count").as[DownloadCount]
          createdAt          <- c.downField("created_at").as[CreatedAt]
          updatedAt          <- c.downField("updated_at").as[UpdatedAt]
          uploader           <- c.downField("uploader").as[GitHub.User]
        } yield Asset(
          id,
          url,
          browserDownloadUrl,
          name,
          label,
          state,
          contentType,
          size,
          downloadCount,
          createdAt,
          updatedAt,
          uploader,
        )

    final case class FailedAssetUpload(file: File, cause: Option[GitHubError])
  }

  final case class Response(
    id: Response.Id,
    uri: Response.Url,
    assetsUrl: Response.AssetsUrl,
    uploadUrl: Response.UploadUrl,
    author: GitHub.User,
    tagName: Git.TagName,
    name: ReleaseName,
    body: Description,
    draft: Draft,
    prerelease: Prerelease,
    createdAt: Response.CreatedAt,
    publishedAt: Option[Response.PublishedAt],
    assets: List[Asset],
  )

  object Response {

    type Id = Id.Type
    object Id extends Newtype[Long], CirceNewtypeCodec[Long] {
      extension (a: Id) def id: Long = a.value
    }

    type Url = Url.Type
    object Url extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: Url) def url: String = a.value
    }

    type AssetsUrl = AssetsUrl.Type
    object AssetsUrl extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: AssetsUrl) def assetsUrl: String = a.value
    }

    type UploadUrl = UploadUrl.Type
    object UploadUrl extends Newtype[String], CirceNewtypeCodec[String] {
      extension (a: UploadUrl) def uploadUrl: String = a.value
    }

    type CreatedAt = CreatedAt.Type
    object CreatedAt extends Newtype[Instant], CirceNewtypeCodec[Instant] {
      extension (a: CreatedAt) def createdAt: Instant = a.value
    }

    type PublishedAt = PublishedAt.Type
    object PublishedAt extends Newtype[Instant], CirceNewtypeCodec[Instant] {
      extension (a: PublishedAt) def publishedAt: Instant = a.value
    }

    given encoder: Encoder[Response] =
      response =>
        Json.obj(
          "id"           -> response.id.id.asJson,
          "url"          -> response.uri.url.asJson,
          "assets_url"   -> response.assetsUrl.assetsUrl.asJson,
          "upload_url"   -> response.uploadUrl.uploadUrl.asJson,
          "author"       -> response.author.asJson,
          "tag_name"     -> response.tagName.asJson,
          "name"         -> response.name.asJson,
          "body"         -> response.body.asJson,
          "draft"        -> response.draft.asJson,
          "prerelease"   -> response.prerelease.asJson,
          "created_at"   -> response.createdAt.asJson,
          "published_at" -> response.publishedAt.asJson,
          "assets"       -> response.assets.asJson,
        )

    given decoder: Decoder[Response] =
      c =>
        for {
          id          <- c.downField("id").as[Id]
          url         <- c.downField("url").as[Url]
          assetsUrl   <- c.downField("assets_url").as[AssetsUrl]
          uploadUrl   <- c.downField("upload_url").as[UploadUrl]
          author      <- c.downField("author").as[GitHub.User]
          tagName     <- c.downField("tag_name").as[Git.TagName]
          name        <- c.downField("name").as[ReleaseName]
          body        <- c.downField("body").as[Description]
          draft       <- c.downField("draft").as[Draft]
          prerelease  <- c.downField("prerelease").as[Prerelease]
          createdAt   <- c.downField("created_at").as[Response.CreatedAt]
          publishedAt <- c.downField("published_at").as[Option[Response.PublishedAt]]
          assets      <- c.downField("assets").as[List[Asset]]
        } yield Response(
          id,
          url,
          assetsUrl,
          uploadUrl,
          author,
          tagName,
          name,
          body,
          draft,
          prerelease,
          createdAt,
          publishedAt,
          assets,
        )

  }

}
