package kevinlee.http

import cats.syntax.all._
import hedgehog._
import hedgehog.runner._
import kevinlee.http.HttpResponse.FailedResponseBodyJson

/** @author Kevin Lee
  * @since 2022-03-05
  */
object HttpResponseSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "FailedResponseBodyJson: test JSON encoder",
      FailedResponseBodyJsonSpec.testFailedResponseBodyJsonEncoding
    ),
    property(
      "FailedResponseBodyJson: test JSON decoder",
      FailedResponseBodyJsonSpec.testFailedResponseBodyJsonDecoding
    ),
    example(
      "FailedResponseBodyJson: example test JSON decoder",
      FailedResponseBodyJsonSpec.exampleTestFailedResponseBodyJsonDecoding
    ),
    property(
      "FailedResponseBodyJson: roundtrip test JSON encoder and decoder",
      FailedResponseBodyJsonSpec.roundtripTestFailedResponseBodyJson
    )
  )

  object FailedResponseBodyJsonSpec {

    def testFailedResponseBodyJsonEncoding: Property = for {
      message <- genMessage.log("message")
      errors  <- genErrors.log("errors")

      documentationUrl <- genUrl.option.log("documentationUrl")
    } yield {
      val input = FailedResponseBodyJson(message, errors.map(FailedResponseBodyJson.Errors(_)), documentationUrl)

      import io.circe.syntax._

      val errorsString =
        errors.map(_.map { case (k, v) => raw""""$k":"$v"""" }.mkString("{", ",", "}")).mkString("[", ",", "]")
      val docUrlString = documentationUrl.fold("")(url => raw""","documentation_url":"$url"""")
      val expected     =
        s"""{"message":"$message","errors":$errorsString$docUrlString}""".stripMargin

      val actual = input.asJson.noSpaces
      actual ==== expected
    }

    def testFailedResponseBodyJsonDecoding: Property = for {
      message <- genMessage.log("message")
      errors  <- genErrors.log("errors")

      documentationUrl <- genUrl.option.log("documentationUrl")
    } yield {
      val errorsString =
        errors.map(_.map { case (k, v) => raw""""$k":"$v"""" }.mkString("{", ",", "}")).mkString("[", ",", "]")
      val docUrlString = documentationUrl.fold("")(url => raw""","documentation_url":"$url"""")
      val input        =
        s"""{"message":"$message","errors":$errorsString$docUrlString}""".stripMargin

      val expected = FailedResponseBodyJson(message, errors.map(FailedResponseBodyJson.Errors(_)), documentationUrl)

      import io.circe.Error
      import io.circe.parser._

      val actual = decode[FailedResponseBodyJson](input)
      actual ==== expected.asRight[Error]
    }

    def exampleTestFailedResponseBodyJsonDecoding: Result = {
      val input =
        """{"message":"Validation Failed","errors":[{"resource":"Release","code":"already_exists","field":"tag_name"}],"documentation_url":"https://docs.github.com/rest/reference/repos#create-a-release"}"""

      val expected = FailedResponseBodyJson(
        "Validation Failed",
        List(
          FailedResponseBodyJson.Errors(
            Map(
              "resource" -> "Release",
              "code"     -> "already_exists",
              "field"    -> "tag_name"
            )
          )
        ),
        "https://docs.github.com/rest/reference/repos#create-a-release".some
      )

      import io.circe.Error
      import io.circe.parser._

      val actual = decode[FailedResponseBodyJson](input)
      actual ==== expected.asRight[Error]
    }

    def roundtripTestFailedResponseBodyJson: Property = for {
      message <- genMessage.log("message")
      errors  <- genErrors.log("errors")

      documentationUrl <- genUrl.option.log("documentationUrl")
    } yield {
      val expected = FailedResponseBodyJson(message, errors.map(FailedResponseBodyJson.Errors(_)), documentationUrl)
      import io.circe.Error
      import io.circe.parser._
      import io.circe.syntax._

      val json   = expected.asJson.noSpaces
      val actual = decode[FailedResponseBodyJson](json)
      actual ==== expected.asRight[Error]
    }

    private def genMessage: Gen[String] =
      Gen.string(Gen.alphaNum, Range.linear(1, 20))

    private def genUrl: Gen[String] =
      Gen.string(Gen.alphaNum, Range.linear(1, 20))

    private def genErrors: Gen[List[Map[String, String]]] =
      Gen
        .string(Gen.alphaNum, Range.linear(1, 10))
        .flatMap { k =>
          Gen.string(Gen.alphaNum, Range.linear(1, 20)).map { v =>
            k -> v
          }

        }
        .list(Range.linear(1, 5))
        .map(_.toMap)
        .list(Range.linear(0, 10))

  }

}
