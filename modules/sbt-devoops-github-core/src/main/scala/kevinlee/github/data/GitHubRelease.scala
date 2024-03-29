package kevinlee.github.data

import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import io.estatico.newtype.macros.{newsubtype, newtype}
import kevinlee.git.Git

import java.io.File
import java.time.Instant

/** @author Kevin Lee
  * @since 2021-01-16
  */
@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  )
)
object GitHubRelease {
  final case class CreateRequestParams(
    tagName: Git.TagName,
    name: Option[ReleaseName],
    body: Option[Description],
    draft: Draft,
    prerelease: Prerelease,
  )
  object CreateRequestParams {
    implicit val encoder: Encoder[CreateRequestParams] =
      requestParams =>
        Json.obj(
          (List("tag_name" -> requestParams.tagName.asJson) ++
            requestParams.name.toList.map(name => "name" -> name.asJson) ++
            requestParams.body.toList.map(body => "body" -> body.asJson) ++
            List(
              "draft"      -> requestParams.draft.asJson,
              "prerelease" -> requestParams.prerelease.asJson,
            )): _*
        )

    implicit val decoder: Decoder[CreateRequestParams] =
      c =>
        for {
          tagName    <- c.downField("tag_name").as[Git.TagName]
          name       <- c.downField("name").as[Option[ReleaseName]]
          body       <- c.downField("body").as[Option[Description]]
          draft      <- c.downField("draft").as[Draft]
          prerelease <- c.downField("prerelease").as[Prerelease]
        } yield CreateRequestParams(tagName, name, body, draft, prerelease)

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
    implicit val encoder: Encoder[UpdateRequestParams] =
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
          ): _*
        )

    implicit val decoder: Decoder[UpdateRequestParams] =
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

  final case class UploadAssetParams(
    releaseId: ReleaseId,
    name: UploadAssetParams.AssetName,
    label: Option[UploadAssetParams.AssetLabel],
    assetFile: UploadAssetParams.AssetFile,
  )
  object UploadAssetParams {
    @newtype final case class AssetName(assetName: String)
    @newtype final case class AssetLabel(assetLabel: String)
    final case class AssetFile(assetFile: File, contentTypes: List[GitHubRelease.Asset.ContentType])

  }

  @newtype final case class Accept(accept: String)
  object Accept {
    implicit val encoder: Encoder[Accept] = deriving
    implicit val decoder: Decoder[Accept] = deriving
  }

  @newtype final case class ReleaseId(releaseId: Long)
  object ReleaseId {
    implicit val encoder: Encoder[ReleaseId] = deriving
    implicit val decoder: Decoder[ReleaseId] = deriving
  }

  @newtype final case class ReleaseName(releaseName: String)
  object ReleaseName {
    implicit val encoder: Encoder[ReleaseName] = deriving
    implicit val decoder: Decoder[ReleaseName] = deriving
  }
  @newtype final case class Description(description: String)
  object Description {
    implicit val encoder: Encoder[Description] = deriving
    implicit val decoder: Decoder[Description] = deriving
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
    @newsubtype case class Id(id: Long)
    object Id {
      implicit val encoder: Encoder[Id] = deriving
      implicit val decoder: Decoder[Id] = deriving
    }
    @newtype final case class Url(url: String)
    object Url {
      implicit val encoder: Encoder[Url] = deriving
      implicit val decoder: Decoder[Url] = deriving
    }
    @newtype final case class BrowserDownloadUrl(browserDownloadUrl: String)
    object BrowserDownloadUrl {
      implicit val encoder: Encoder[BrowserDownloadUrl] = deriving
      implicit val decoder: Decoder[BrowserDownloadUrl] = deriving
    }
    @newtype final case class Name(name: String)
    object Name {
      implicit val encoder: Encoder[Name] = deriving
      implicit val decoder: Decoder[Name] = deriving
    }
    @newtype final case class Label(label: String)
    object Label {
      implicit val encoder: Encoder[Label] = deriving
      implicit val decoder: Decoder[Label] = deriving
    }
    @newtype final case class State(state: String)
    object State {
      implicit val encoder: Encoder[State] = deriving
      implicit val decoder: Decoder[State] = deriving
    }
    @newtype final case class ContentType(contentType: String)
    object ContentType {
      implicit val encoder: Encoder[ContentType] = deriving
      implicit val decoder: Decoder[ContentType] = deriving
    }
    @newtype final case class Size(size: Long)
    object Size {
      implicit val encoder: Encoder[Size] = deriving
      implicit val decoder: Decoder[Size] = deriving
    }
    @newtype final case class DownloadCount(downloadCount: Int)
    object DownloadCount {
      implicit val encoder: Encoder[DownloadCount] = deriving
      implicit val decoder: Decoder[DownloadCount] = deriving
    }
    @newtype final case class CreatedAt(createdAt: Instant)
    object CreatedAt {
      implicit val encoder: Encoder[CreatedAt] = deriving
      implicit val decoder: Decoder[CreatedAt] = deriving
    }
    @newtype final case class UpdatedAt(updatedAt: Instant)
    object UpdatedAt {
      implicit val encoder: Encoder[UpdatedAt] = deriving
      implicit val decoder: Decoder[UpdatedAt] = deriving
    }

    implicit val encoder: Encoder[Asset] =
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
    implicit val decoder: Decoder[Asset] =
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

    @newsubtype case class Id(id: Long)
    object Id {
      implicit val encoder: Encoder[Id] = deriving
      implicit val decoder: Decoder[Id] = deriving
    }
    @newtype final case class Url(url: String)
    object Url {
      implicit val encoder: Encoder[Url] = deriving
      implicit val decoder: Decoder[Url] = deriving
    }
    @newtype final case class AssetsUrl(assetsUrl: String)
    object AssetsUrl {
      implicit val encoder: Encoder[AssetsUrl] = deriving
      implicit val decoder: Decoder[AssetsUrl] = deriving
    }
    @newtype final case class UploadUrl(uploadUrl: String)
    object UploadUrl {
      implicit val encoder: Encoder[UploadUrl] = deriving
      implicit val decoder: Decoder[UploadUrl] = deriving
    }
    @newtype final case class CreatedAt(createdAt: Instant)
    object CreatedAt {
      implicit val encoder: Encoder[CreatedAt] = deriving
      implicit val decoder: Decoder[CreatedAt] = deriving
    }
    @newtype final case class PublishedAt(publishedAt: Instant)
    object PublishedAt {
      implicit val encoder: Encoder[PublishedAt] = deriving
      implicit val decoder: Decoder[PublishedAt] = deriving
    }

    implicit val encoder: Encoder[Response] =
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

    implicit val decoder: Decoder[Response] =
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
