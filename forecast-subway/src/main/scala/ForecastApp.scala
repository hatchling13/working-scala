
import zio._
// 시스템 시간 API (JAVA ^8)
import java.time.LocalDateTime

// webhook 라이브러리
import sttp.client3._
// URI instance를 생성하는 모듈
// https://sttp.softwaremill.com/en/stable/model/uri.html#uri-interpolator
import sttp.model.UriInterpolator

// json을 다루는 라이브러리인 ujson은 sttp와 intergrate 되지 않아 zio-json을 별도로 build.sbt에 추가해야 합니다
// 기존에 만들었던 readJson 메서드를 사용하기 위해 필요합니다
// // https://sttp.softwaremill.com/en/stable/json.html?highlight=json#zio-json
import ujson.Value.Value


abstract class SimpleError(message: String = "", cause: Throwable = null) extends Throwable(message, cause) with Product with Serializable

object SimpleError {
  final case class ReadFail(cause: Throwable) extends SimpleError(s"read fail: ", cause)
  final case class FindDataFail(cause: Throwable) extends SimpleError(s"찾지 못했어요", cause)
}

object ForecastApp extends ZIOAppDefault {

  def getEnvVariable(name: String) = {
    val parsedName = name match {
      case "serviceKey" => ZIO.succeed(name)
      case "webhookKey" => ZIO.succeed(name)
      case _ => ZIO.fail(name)
    }
    parsedName.map(validName => System.property(validName))
  }

  val ZIOserviceKey = getEnvVariable("serviceKey")
  val ZIOwebhookKey = getEnvVariable("webhookKey")
  // val a = ZIOserviceKey.flatten
  // val b = ZIOserviceKey.flatten.flatMap(x=> ZIO.fromOption(x))

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =

    for {
      serviceKey <- ZIOserviceKey.flatMap(x => x.flatMap(y => ZIO.fromOption(y)))

      _ <- zio.Console.printLine("main")
      weatherJsonString = getWeatherData(serviceKey)
      weatherJson <- readJson(weatherJsonString)
      _ <- zio.Console.printLine(weatherJson)
      ptyValue <- findByCategory(weatherJson, "PTY")
      _ <- zio.Console.printLine(ptyValue("fcstValue"))

      // PTY는 강수형태를 의미합니다(Open API 가이드 문서 부록 참조(16p)
      ptyValue <- findByCategory(weatherJson, "PTY")
      _ = sendDiscordMessage(createMessage(convertPtscValue(ptyValue("fcstValue").value.toString())))
    } yield ()

  def convertPtscValue(value: String): String = {
    value match {
      case "0" => "맑음"
      case "1" => "비"
      case "2" => "비/눈"
      case "3" => "눈"
      case "5" => "빗방울"
      case "6" => "빗방울/눈날림"
      case "7" => "눈날림"
      case _ => "알수없음"
    }
  }

  // 기상청 Open API로 요청을 날려서 받은 JSON Response를 특정 카테고리만 filter 해주는 함수
  def findByCategory(json: Value, category: String): ZIO[Any, SimpleError, Value] = {
    ZIO.attempt(
      json("response")("body")("items")("item").arr.filter(item => item("category").str == category).head
    ).catchAll(x => ZIO.fail(SimpleError.FindDataFail(x)))
  }

  // Discord Message 생성 함수
  def createMessage(skyString: String): String = {
    "마포구 공덕동은 곧" + skyString + " 예정 입니다."
  }

  // Deep ZIO 1에서 작성한 JSON 읽는 코드
  // https://spiny-entree-7e0.notion.site/DeepZIO-1-00fe9e7a783640a197e35788a243c090
  def readJson(jsonString: String): ZIO[Any, SimpleError, Value] =
    for {
      json <- ZIO
        .attempt(ujson.read(jsonString))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
    } yield json

  // 기상청 Open API에 요청을 날리는 함수
  // serviceKey는 시스템 환경변수로 런타임에 바인딩됩니다
  def getWeatherData(serviceKey: String): String = {
    // 하루 8번 날씨가 update되므로 최소 간격인 3시간으로 설정
    val now = LocalDateTime.now().minusHours(3)
    val date = now.getYear.toString + pad2(now.getMonth.getValue) + pad2(now.getDayOfMonth)
    val hour = pad2(now.getHour)
    val minute = pad2(now.getMinute)
    val uri = s"http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=${serviceKey}&dataType=JSON&numOfRows=1000&pageNo=1&base_date=${date}&base_time=${hour}${minute}&nx=60&ny=126"
    print(uri, "uri")
    basicRequest
      .get(uri"${uri}")
      .send(HttpClientSyncBackend())
      .body.getOrElse("{}")
  }

  // JAVA.time format과 기상청 Open API 시간 format이 달라 padding 처리를 해주는 함수
  // 예시) 7시 -> 07시
  def pad2(num: Int): String = {
    val str = num.toString
    if (str.length == 1) {
      return "0" + str
    }
    return str
  }

  // 디스코드 웹 훅을 테스트 할 수 있는 사이트
  // https://discohook.org/?data=eyJtZXNzYWdlcyI6W3siZGF0YSI6eyJjb250ZW50IjoiSGV5LCB3ZWxjb21lIHRvIDw6ZGlzY29ob29rOjczNjY0ODM5ODA4MTYyMjAxNj4gKipEaXNjb2hvb2sqKiEgVGhlIGVhc2llc3Qgd2F5IHRvIHBlcnNvbmFsaXNlIHlvdXIgRGlzY29yZCBzZXJ2ZXIuXG5cblRoZXJlJ3MgbW9yZSBpbmZvIGJlbG93LCBidXQgeW91IGRvbid0IGhhdmUgdG8gcmVhZCBpdC4gSWYgeW91J3JlIHJlYWR5IHByZXNzICoqQ2xlYXIgQWxsKiogaW4gdGhlIHRvcCBvZiB0aGUgZWRpdG9yIHRvIGdldCBzdGFydGVkLlxuXG5EaXNjb2hvb2sgaGFzIGEgW3N1cHBvcnQgc2VydmVyXShodHRwczovL2Rpc2NvaG9vay5hcHAvZGlzY29yZCksIGlmIHlvdSBuZWVkIGhlbHAgZmVlbCBmcmVlIHRvIGpvaW4gaW4gYW5kIGFzayBxdWVzdGlvbnMsIHN1Z2dlc3QgZmVhdHVyZXMsIG9yIGp1c3QgY2hhdCB3aXRoIHRoZSBjb21tdW5pdHkuXG5cbldlIGFsc28gaGF2ZSBbY29tcGxlbWVudGFyeSBib3RdKGh0dHBzOi8vZGlzY29ob29rLmFwcC9ib3QpIHRoYXQgbWF5IGhlbHAgb3V0LCBmZWF0dXJpbmcgcmVhY3Rpb24gcm9sZXMgYW5kIG90aGVyIHV0aWxpdGllcy5cbl8gXyIsImVtYmVkcyI6W3sidGl0bGUiOiJXaGF0J3MgdGhpcyBhYm91dD8iLCJkZXNjcmlwdGlvbiI6IkRpc2NvaG9vayBpcyBhIGZyZWUgdG9vbCB0aGF0IGFsbG93cyB5b3UgdG8gcGVyc29uYWxpc2UgeW91ciBzZXJ2ZXIgdG8gbWFrZSB5b3VyIHNlcnZlciBzdGFuZCBvdXQgZnJvbSB0aGUgY3Jvd2QuIFRoZSBtYWluIHdheSBpdCBkb2VzIHRoaXMgaXMgdXNpbmcgW3dlYmhvb2tzXShodHRwczovL3N1cHBvcnQuZGlzY29yZC5jb20vaGMvZW4tdXMvYXJ0aWNsZXMvMjI4MzgzNjY4KSwgd2hpY2ggYWxsb3dzIHNlcnZpY2VzIGxpa2UgRGlzY29ob29rIHRvIHNlbmQgYW55IG1lc3NhZ2VzIHdpdGggZW1iZWRzIHRvIHlvdXIgc2VydmVyLlxuXG5UbyBnZXQgc3RhcnRlZCB3aXRoIHNlbmRpbmcgbWVzc2FnZXMsIHlvdSBuZWVkIGEgd2ViaG9vayBVUkwsIHlvdSBjYW4gZ2V0IG9uZSB2aWEgdGhlIFwiSW50ZWdyYXRpb25zXCIgdGFiIGluIHlvdXIgc2VydmVyJ3Mgc2V0dGluZ3MuIElmIHlvdSdyZSBoYXZpbmcgaXNzdWVzIGNyZWF0aW5nIGEgd2ViaG9vaywgW3RoZSBib3RdKGh0dHBzOi8vZGlzY29ob29rLmFwcC9ib3QpIGNhbiBoZWxwIHlvdSBjcmVhdGUgb25lIGZvciB5b3UuXG5cbktlZXAgaW4gbWluZCB0aGF0IERpc2NvaG9vayBjYW4ndCBkbyBhdXRvbWF0aW9uIHlldCwgaXQgb25seSBzZW5kcyBtZXNzYWdlcyB3aGVuIHlvdSB0ZWxsIGl0IHRvLiBJZiB5b3UgYXJlIGxvb2tpbmcgZm9yIGFuIGF1dG9tYXRpYyBmZWVkIG9yIGN1c3RvbSBjb21tYW5kcyB0aGlzIGlzbid0IHRoZSByaWdodCB0b29sIGZvciB5b3UuIiwiY29sb3IiOjU4MTQ3ODN9LHsidGl0bGUiOiJEaXNjb3JkIGJvdCIsImRlc2NyaXB0aW9uIjoiRGlzY29ob29rIGhhcyBhIGJvdCBhcyB3ZWxsLCBpdCdzIG5vdCBzdHJpY3RseSByZXF1aXJlZCB0byBzZW5kIG1lc3NhZ2VzIGl0IG1heSBiZSBoZWxwZnVsIHRvIGhhdmUgaXQgcmVhZHkuXG5cbkJlbG93IGlzIGEgc21hbGwgYnV0IGluY29tcGxldGUgb3ZlcnZpZXcgb2Ygd2hhdCB0aGUgYm90IGNhbiBkbyBmb3IgeW91LiIsImNvbG9yIjo1ODE0NzgzLCJmaWVsZHMiOlt7Im5hbWUiOiJHZXR0aW5nIHNwZWNpYWwgZm9ybWF0dGluZyBmb3IgbWVudGlvbnMsIGNoYW5uZWxzLCBhbmQgZW1vamkiLCJ2YWx1ZSI6IlRoZSAqKi9mb3JtYXQqKiBjb21tYW5kIG9mIHRoZSBib3QgY2FuIGdpdmUgeW91IHNwZWNpYWwgZm9ybWF0dGluZyBmb3IgdXNlIGluIERpc2NvcmQgbWVzc2FnZXMgdGhhdCBsZXRzIHlvdSBjcmVhdGUgbWVudGlvbnMsIHRhZyBjaGFubmVscywgb3IgdXNlIGVtb2ppIHJlYWR5IHRvIHBhc3RlIGludG8gdGhlIGVkaXRvciFcblxuVGhlcmUgYXJlIFttYW51YWwgd2F5c10oaHR0cHM6Ly9kaXNjb3JkLmRldi9yZWZlcmVuY2UjbWVzc2FnZS1mb3JtYXR0aW5nKSBvZiBkb2luZyB0aGlzLCBidXQgaXQncyB2ZXJ5IGVycm9yIHByb25lLiBUaGUgYm90IHdpbGwgbWFrZSBzdXJlIHlvdSdsbCBhbHdheXMgZ2V0IHRoZSByaWdodCBmb3JtYXR0aW5nIGZvciB5b3VyIG5lZWRzLiJ9LHsibmFtZSI6IkNyZWF0aW5nIHJlYWN0aW9uIHJvbGVzIiwidmFsdWUiOiJZb3UgY2FuIG1hbmFnZSByZWFjdGlvbiByb2xlcyB3aXRoIHRoZSBib3QgdXNpbmcgdGhlICoqL3JlYWN0aW9uLXJvbGUqKiBjb21tYW5kLlxuXG5UaGUgc2V0LXVwIHByb2Nlc3MgaXMgdmVyeSBpbnR1aXRpdmU6IHR5cGUgb3V0ICoqL3JlYWN0aW9uLXJvbGUgY3JlYXRlKiosIHBhc3RlIGEgbWVzc2FnZSBsaW5rLCBzZWxlY3QgYW4gZW1vamksIGFuZCBwaWNrIGEgcm9sZS4gSGl0IGVudGVyIGFuZCB5b3UncmUgZG9uZSwgeW91ciBtZW1iZXJzIGNhbiBub3cgcmVhY3QgdG8gYW55IG9mIHlvdXIgbWVzc2FnZXMgdG8gcGljayB0aGVpciByb2xlcy4ifSx7Im5hbWUiOiJSZWNvdmVyIERpc2NvaG9vayBtZXNzYWdlcyBmcm9tIHlvdXIgc2VydmVyIiwidmFsdWUiOiJJdCBjYW4gYWxzbyByZXN0b3JlIGFueSBtZXNzYWdlIHNlbnQgaW4geW91ciBEaXNjb3JkIHNlcnZlciBmb3IgeW91IHZpYSB0aGUgYXBwcyBtZW51LlxuXG5UbyBnZXQgc3RhcnRlZCwgcmlnaHQtY2xpY2sgb3IgbG9uZy1wcmVzcyBvbiBhbnkgbWVzc2FnZSBpbiB5b3VyIHNlcnZlciwgcHJlc3Mgb24gYXBwcywgYW5kIHRoZW4gcHJlc3MgKipSZXN0b3JlIHRvIERpc2NvaG9vayoqLiBJdCdsbCBzZW5kIHlvdSBhIGxpbmsgdGhhdCBsZWFkcyB0byB0aGUgZWRpdG9yIHBhZ2UgY29udGFpbmluZyB0aGUgbWVzc2FnZSB5b3Ugc2VsZWN0ZWQhIn0seyJuYW1lIjoiT3RoZXIgZmVhdHVyZXMiLCJ2YWx1ZSI6IkRpc2NvaG9vayBjYW4gYWxzbyBncmFiIGltYWdlcyBmcm9tIHByb2ZpbGUgcGljdHVyZXMgb3IgZW1vamksIG1hbmFnZSB5b3VyIHdlYmhvb2tzLCBhbmQgbW9yZS4gSW52aXRlIHRoZSBib3QgYW5kIHVzZSAqKi9oZWxwKiogdG8gbGVhcm4gYWJvdXQgYWxsIHRoZSBib3Qgb2ZmZXJzISJ9XX1dLCJhdHRhY2htZW50cyI6W119fV19
  // sttp 문서
  // https://sttp.softwaremill.com/en/stable/quickstart.html#imports
  def sendDiscordMessage(message: String): Unit = {
    val requestPayLoad = {
      s"""
  {"content":"${message}","embeds":null,"username":"서브웨이 팀 봇","attachments":[]}
  """.stripMargin
    }

    val backend = HttpClientSyncBackend()

    for {
      webhookKey <- ZIOwebhookKey.flatMap(x => x.flatMap(y => ZIO.fromOption(y)))
      uri = UriInterpolator.interpolate(StringContext(s"https://discord.com/api/webhooks/${webhookKey}"))
      _ <- Console.printLine(s"${uri}")
      _ <- ZIO.attempt(basicRequest
      .body(requestPayLoad)
      .header("Content-Type", "application/json", replaceExisting = true)
      .post(uri)
      .send(backend))
    } yield ()


  }
}
