package kevinlee.github.data

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import io.circe.refined._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.estatico.newtype.macros.{newsubtype, newtype}

/** @author Kevin Lee
  * @since 2021-01-03
  */
final case class User(
  id: User.Id,
  login: User.Login,
  url: User.Url,
  name: Option[User.Name],
  avatarUrl: Option[User.AvatarUrl],
)

@SuppressWarnings(
  Array(
    "org.wartremover.warts.ExplicitImplicitTypes",
    "org.wartremover.warts.ImplicitConversion",
    "org.wartremover.warts.ImplicitParameter",
    "org.wartremover.warts.PublicInference",
  )
)
object User {
  @newsubtype case class Id(id: Long)
  object Id        {
    implicit val encoder: Encoder[Id] = deriving
    implicit val decoder: Decoder[Id] = deriving
  }
  @newtype case class Login(login: String Refined NonEmpty)
  object Login     {
    implicit val encoder: Encoder[Login] = deriving
    implicit val decoder: Decoder[Login] = deriving
  }
  @newtype case class Url(url: String Refined string.Url)
  object Url       {
    implicit val encoder: Encoder[Url] = deriving
    implicit val decoder: Decoder[Url] = deriving
  }
  @newtype case class Name(name: String)
  object Name      {
    implicit val encoder: Encoder[Name] = deriving
    implicit val decoder: Decoder[Name] = deriving
  }
  @newtype case class AvatarUrl(avatarUrl: String)
  object AvatarUrl {
    implicit val encoder: Encoder[AvatarUrl] = deriving
    implicit val decoder: Decoder[AvatarUrl] = deriving
  }

  implicit final val encoder: Encoder[User] =
    author =>
      Json.obj(
        (
          List(
            "id"    -> author.id.asJson,
            "login" -> author.login.asJson,
            "url"   -> author.url.asJson,
          ) ++
            author
              .name
              .toList
              .map(name => "name" -> name.asJson) ++
            author
              .avatarUrl
              .toList
              .map(avatarUrl => "avatar_url" -> avatarUrl.asJson)
        ): _*
      )
  implicit final val decoder: Decoder[User] =
    (c: HCursor) =>
      for {
        id        <- c.downField("id").as[Id]
        login     <- c.downField("login").as[Login]
        url       <- c.downField("url").as[Url]
        name      <- c.downField("name").as[Option[Name]]
        avatarUrl <- c.downField("avatar_url").as[Option[AvatarUrl]]
      } yield User(id, login, url, name, avatarUrl)

}
