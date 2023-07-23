import sttp.client4._
import sttp.client4.ziojson._
import zio.{ZIO, ZIOAppDefault}
import zio.json._
import sttp.client4.httpclient.HttpClientSyncBackend
import ujson.Value
import kantan.csv._
import kantan.csv.ops._
import java.io.File

object DiscordWebhook extends ZIOAppDefault {

    // 기본적인 csv 파일의 경로와 API 키를 설정합니다.
    private val csvPath = "../Forecast/src/main/scala/api.csv"
    private val weatherAPI = "WEATHER_API"
    private val discord = "DISCORD_WEBHOOK_KEY"

    val run = for {
        _ <- zio.Console.printLine("Start")
        // 지역 정보를 가져옵니다보
        regionMap <- ZIO.attempt(getRegionMap)
        // 날씨 정보를 가져옵니다.
        weatherData <- weatherInfo(regionMap)
        _ <- zio.Console.printLine(weatherData)

        // 정보를 가져온 후, 디스코드로 전송합니다보
        _ <- sendToDiscord(weatherData)
    } yield ()


    val backend = HttpClientSyncBackend()

    // Discord Webhook API를 위한 JSON Encoder와 Decoder를 생성합니다.
    implicit val payloadJsonEncoder: JsonEncoder[RequestPayload] = DeriveJsonEncoder.gen[RequestPayload]
    implicit val myResponseJsonDecoder: JsonDecoder[ResponsePayload] = DeriveJsonDecoder.gen[ResponsePayload]

    // JSON 문자열을 파싱하는 함수입니다.
    def readJson(jsonString: String): ZIO[Any, Throwable, Value] =
        ZIO.attempt(ujson.read(jsonString))
        .catchAll(e => ZIO.fail(new Exception(s"Failed to parse json: $e")))

    // JSON 객체에서 특정 키를 가진 값을 가져오는 함수입니다.
    def getKeyFromJson(item: ujson.Value, key: String): Option[String] = 
        item.obj.get(key).map(v => v match {
        case ujson.Str(s) => s
        case _ => ""
    })

    // JSON 객체에서 특정 키를 가진 값을 가져오는 함수입니다. 
    // 이 함수는 Int 타입의 값을 가져오고, 여기서는 `nx`와 `ny`를 가져올 때 사용합니다.
    def getFromJsonInt(item: ujson.Value, key: String): Option[Int] = 
        item.obj.get(key).map(v => v match {
        case ujson.Num(n) => n.toInt
        case _ => 0
    })

    // 날짜와 시간을 포맷팅하는 함수입니다. 포멧팅 형식은 다음과 같습니다.
    // 날짜: YYYY-MM-DD, 시간: HH:MM
    //
    // 예를 들어, 20210801을 2021-08-01로, 0200을 02:00으로 변환합니다.
    // 따라서 출력되는 값은 2021-08-01, 02:00입니다.
    def formatDate(date: String): String = {
        val year = date.substring(0, 4)
        val month = date.substring(4, 6)
        val day = date.substring(6, 8)
        s"$year-$month-$day"
    }

    def formatTime(time: String): String = {
        val hour = time.substring(0, 2)
        val minute = time.substring(2, 4)
        s"$hour:$minute"
    }

    // 날씨 API에서 가져온 JSON 객체를 파싱하는 함수입니다.
    // TODO: refactor this function
    def getWeatherInfo(json: Value, regionMap: Map[(String, String), String]) =
    ZIO.fromOption(json.obj.get("response"))
        .flatMap(response => ZIO.fromOption(response.obj.get("body")))
        .flatMap(body => ZIO.fromOption(body.obj.get("items")))
        .flatMap(items => ZIO.fromOption(items.obj.get("item").collect {
            case ujson.Arr(itemArray) => itemArray
        }))
        .map(items => items.map(item => {
            val category = getKeyFromJson(item, "category").map(getWeatherStatus).getOrElse("Unknown Category")
            val fcstDate = getKeyFromJson(item, "fcstDate").map(formatDate).getOrElse("")
            val fcstTime = getKeyFromJson(item, "fcstTime").map(formatTime).getOrElse("")
            val fcstValue = getKeyFromJson(item, "fcstValue").getOrElse("")
            val nx = getFromJsonInt(item, "nx").getOrElse(0)
            val ny = getFromJsonInt(item, "ny").getOrElse(0)
            val region = regionMap.getOrElse((nx.toString(), ny.toString()), "Unknown region")
            s"지역(동): $region, 카테고리: $category, 날짜: $fcstDate, 시간: $fcstTime, 강수 정보: ${getRainStatus(fcstValue)}"
        // `.take`는 `ZIO`의 메서드로, `ZIO`의 결과를 가져올 때 사용합니다.
        }).take(5))
        .map(_.mkString("\n"))
        .orElseFail(new Exception("item array not found or invalid"))

    // 날씨 API에서 가져온 카테고리를 변환하는 함수입니다.
    // 예를 들어, `LGT`는 `낙뢰`로, `PTY`는 `강수형태`로 변환합니다.
    def getWeatherStatus(category: String): String = category match {
        case "LGT" => "낙뢰"
        case "PTY" => "강수형태"
        case "RN1" => "1시간 강수량"
        case "SKY" => "하늘상태"
        case _ => "Unknown category"
    }


    // 날씨 API에서 가져온 강수 확률을 변환하는 함수입니다.
    def getRainStatus(fcstValue: String): String = if (fcstValue == "0") "강수 없음" else "강수 있음"

    // 날씨 API를 호출해서 날씨 정보를 가져오는 함수입니다.
    def weatherInfo(regionMap: Map[(String, String), String]) = for {
        response <- ZIO.fromEither(
            basicRequest
                .get(uri"$weatherAPI")
                .response(asString).send(backend).body
        )
        json <- readJson(response)
        weatherData <- getWeatherInfo(json, regionMap)
    } yield weatherData

    def sendToDiscord(weatherData: String) = {
        val requestPayload = RequestPayload(weatherData)
        val response: Identity[Response[Either[ResponseException[String, String], ResponsePayload]]] =
            basicRequest
                .post(uri"$discord")
                .body(requestPayload)
                .response(asJson[ResponsePayload])
                .send(backend)
        ZIO.fromEither(response.body)
    }

    // CSV 파일에서 지역 정보를 가져오는 함수입니다.
    // CSV 파일은 다음과 같은 형식으로 구성되어 있습니다.
    // 격자 X,격자 Y,3단계
    // `격자 X`와 `격자 Y`는 날씨 API에서 `nx`와 `ny`에 각각 대응합니다.
    //
    // 이 정보를 이용해 날씨 API에서 가져온 `nx`와 `ny`를 지역 정보(동)로 변환합니다.
    def getRegionMap: Map[(String, String), String] = {
        val reader = new File(csvPath).asCsvReader[RegionData](rfc.withHeader)
        
        reader.collect {
            case Right(data) => (data.nx, data.ny) -> data.dong
        }.toList.toMap
    }

    // Discord Webhook API를 위한 Request와 Response 객체입니다.
    // `RequestPayload`는 Discord Webhook API로 전송할 데이터를 담고 있습니다.
    // `ResponsePayload`는 Discord Webhook API로부터 받은 응답을 담고 있습니다.
    case class RequestPayload(content: String)
    case class ResponsePayload(data: String)
}

// 지역 정보를 처리하기 위한 클래스입니다.
case class RegionData(nx: String, ny: String, dong: String)

// 지역 정보를 CSV 파일에서 읽어오기 위한 코드입니다.
object RegionData {
    implicit val headerDecoder: HeaderDecoder[RegionData] = HeaderDecoder.decoder("격자 X", "격자 Y", "3단계")(RegionData.apply _)
}