import zio._
import sttp.client3._
import zio.json._
import sttp.client3.ziojson._
import ujson.Value.Value

import java.time.{LocalDate, LocalTime}
import scala.collection.mutable.ArrayBuffer

object ForecastClient extends ZIOAppDefault{

  case class RequestPayload(content: String)
  case class ResponsePayload(data: String)

  val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

  val today = LocalDate.now.toString.replaceAll("-", "")
  val now = LocalTime.now.toString.replaceAll(":", "").substring(0, 4)
  val url = s"${MyKeyUtil.apiUri}?serviceKey=${MyKeyUtil.key}&dataType=JSON&numOfRows=1000&pageNo=1&base_date=${today}&base_time=${now}&nx=60&ny=126"

  override def run: ZIO[Any with ZIOAppArgs with Scope, Throwable, Unit] = {
    for {
      _ <- ZIO.unit
      response = getApiResponse
      json <- readJson(response.body)
      items = getItemsFromJson(json)
      _ <- sendDiscordMessage(s"공덕 날씨($today)\n$items")
    } yield()
  }

  private def getApiResponse: Identity[Response[String]] = {
    basicRequest
      .get(uri"$url")
      .response(asString.getRight)
      .send(backend)
  }

  def readJson(name: String) =
    for {
      json <- ZIO
        .attempt(ujson.read(name))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json

  def getItemsFromJson(json: Value): String = {
    json("response")("body")("items")("item").arr
      .filter(el => el.obj("category").str == "RN1")
      .map(item => s"${item.obj("fcstTime").str} : ${item.obj("fcstValue").str} \n")
      .mkString
  }

  def sendDiscordMessage(message: String) = {
    val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    implicit val payloadJsonEncoder: JsonEncoder[RequestPayload] = DeriveJsonEncoder.gen[RequestPayload]
    implicit val myResponseJsonDecoder: JsonDecoder[ResponsePayload] = DeriveJsonDecoder.gen[ResponsePayload]

    val requestPayload = RequestPayload(message)
    val response: Identity[Response[Either[ResponseException[String, String], ResponsePayload]]] =
      basicRequest
        .post(uri"${MyKeyUtil.discordUri}")
        .body(requestPayload)
        .response(asJson[ResponsePayload])
        .send(backend)

    ZIO.attempt(response)
      .catchAll(_ => ZIO.fail(new Exception(s"discord send fail")))
  }
}
