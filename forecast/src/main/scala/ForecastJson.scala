import com.typesafe.config.ConfigFactory
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}
import sttp.client3.{HttpClientSyncBackend, _}
import zio.json._
import sttp.client3.ziojson.{asJson, zioJsonBodySerializer}
import ujson.Value.Value

import java.io.File
import scala.collection.mutable.Map
import java.text.SimpleDateFormat


object ForecaseParseApp extends ZIOAppDefault
{
  case class RequestPayload(content: String)
  case class ResponsePayload(data: String)

  val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  val config = ConfigFactory.parseFile(new File(".scalafmt.conf"))

  // name 경로에 접근하여 JSON 데이터 반환
  def readJson(name: String): ZIO[Any, SimpleError.ReadFail, Value] =
    for {
      json <- ZIO
        .attempt(ujson.read(name))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json


  // 날씨 예보 데이터 API 호출 후 결과 반환
  def getWeatherInfo(): Identity[Response[String]] = {
    val uri = config.getString("weatherUri")
    val response =
      basicRequest
        .get(uri"${uri}")
        .response(asString.getRight)
        .send(backend)

    response
  }

  def sendToDiscord(weatherData: String) = {
    implicit val payloadJsonEncoder: JsonEncoder[RequestPayload] = DeriveJsonEncoder.gen[RequestPayload]
    implicit val myResponseJsonDecoder: JsonDecoder[ResponsePayload] = DeriveJsonDecoder.gen[ResponsePayload]

    val uri = config.getString("discordWebhookUri")
    val api = config.getString("discordWebhookApi")

    val requestPayload = RequestPayload(weatherData)
    val response: Identity[Response[Either[ResponseException[String, String], ResponsePayload]]] =
      basicRequest
        .post(uri"${uri}${api}")
        .body(requestPayload)
        .response(asJson[ResponsePayload])
        .send(backend)

    ZIO.attempt(response)
      .catchAll(_ => ZIO.fail(new Exception()))
  }

  def stringToDate(strDate: String): String = {
    val strDateFormatter = new SimpleDateFormat("yyyyMMdd")
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    dateFormatter.format(strDateFormatter.parse(strDate))
  }

  def stringToTime(strTime: String): String = {
    val strTimeFormatter = new SimpleDateFormat("HHmm")
    val timeFormatter = new SimpleDateFormat("HH:mm")

    timeFormatter.format(strTimeFormatter.parse(strTime))
  }

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
  {
    val response: Identity[Response[String]] = getWeatherInfo()

    // JSON 데이터 category별 항목명, 단위
    val categoryMap: Map[String, List[String]]= Map(
      "T1H" -> List("기온", "℃")
      , "RN1" -> List("강수량(1시간)", "mm")
      , "REH" -> List("습도", "%")
      , "VEC" -> List("풍향", "deg")
      , "WSD" -> List("풍속", "m/s")
    )

    for
    {
      _ <- zio.Console.printLine("[Response Data] \n" + response.body)
      json <- readJson(response.body)

      // arr : JSON 데이터를 배열로 변환
      // EX {"baseDate":"20230723","baseTime":"1430" ... }, {"baseDate":"20230723", ... } { ...  }
      // EX data(0)("baseDate") // 20230723
      data = json("response")("body")("items")("item")
        .arr
        .sortBy(d => d.obj("fcstDate").str + d.obj("fcstTime").str)
        .reverse // 날짜 내림차순 정렬
        .filter(d => categoryMap.contains(d.obj("category").str))

      // 가장 최근 시간대의 날씨 데이터
      firstData = data
        .filter(d => d.obj("fcstDate").str + d.obj("fcstTime").str == data(0)("fcstDate").str + data(0)("fcstTime").str)
        .map (
          d =>
              categoryMap(d.obj("category").str)(0) + " : " +
              d.obj("fcstValue").str + " " +
              (if(d.obj("category").str == "RN1" && d.obj("fcstValue").str == "강수없음") "" else categoryMap(d.obj("category").str)(1)) + "\n"
        )

      // 날짜 형식 변환
      date = stringToDate(data(0)("fcstDate").str) + " " + stringToTime(data(0)("fcstTime").str)

      _  <- zio.Console.printLine(s"[${date}]\n${firstData.mkString}")
      _ <- sendToDiscord(s"[${date}]\n${firstData.mkString}")
//    [2023 - 07 - 23 20: 00]
//    풍속: 2 m / s
//    풍향: 183 deg
//    습도: 90 %
//    기온: 26 ℃
//    강수량(1 시간): 강수없음
    } yield()
  }
}
