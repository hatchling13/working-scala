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
    private val csvPath = "../fixture/region_data.csv"
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
    // 예를 들어, 20210801은 각각 2021-08-01, 0200을 02:00으로 변환한 뒤 `2021-08-01, 02:00` 형식으로 반환합니다.
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

    def parseItem(item: ujson.Value, regionMap: Map[(String, String), String]): String = {
        val category = getKeyFromJson(item, "category").map(getWeatherStatus).getOrElse("Unknown Category")
        val fcstDate = getKeyFromJson(item, "fcstDate").map(formatDate).getOrElse("Empty Date")
        val fcstTime = getKeyFromJson(item, "fcstTime").map(formatTime).getOrElse("Empty Time")
        val fcstValue = getKeyFromJson(item, "fcstValue").getOrElse("Empty Value")
        val nx = getFromJsonInt(item, "nx").getOrElse(0)
        val ny = getFromJsonInt(item, "ny").getOrElse(0)
        val region = regionMap.getOrElse((nx.toString(), ny.toString()), "Unknown region")

        s"지역(동): $region, 카테고리: $category, 날짜: $fcstDate, 시간: $fcstTime, 강수 정보: ${getRainStatus(fcstValue)}"
    }

    def getWeatherInfo(json: Value, regionMap: Map[(String, String), String]) =
        for {
            response <- ZIO.fromOption(json.obj.get("response")).mapError(_ => "response not found in json")
            body <- ZIO.fromOption(response.obj.get("body")).mapError(_ => "body not found in json")
            items <- ZIO.fromOption(body.obj.get("items")).mapError(_ => "items not found in json")
            
            itemArray <- ZIO.fromOption(items.obj.get("item").collect {
                case ujson.Arr(itemArray) => itemArray
            }).mapError(_ => "item array not found or invalid")
        // `take`는 `Iterable`을 구현한 클래스(List, Array, Vector 등)에서 사용할 수 있는 메서드입니다.
        // 여기서는 `map`에서 변환된 `itemArray`가 `Iterable`을 구현한 클래스이기 때문에 사용할 수 있습니다.
        //
        // `take(n)`은 `Iterable`에서 앞에서부터 `n`개의 요소를 가져옵니다.
        } yield itemArray.take(5).map(item => parseItem(item, regionMap)).mkString("\n")

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
            // ref: https://zio.dev/reference/core/zio/#from-side-effects
            ZIO.attempt {
                val response: Identity[Response[Either[ResponseException[String, String], ResponsePayload]]] =
                    basicRequest
                        .post(uri"$discord")
                        .body(requestPayload)
                        .response(asJson[ResponsePayload])
                        .send(backend)

                response.body
            }.flatMap(ZIO.fromEither(_))
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

// 지역 정보를 CSV 파일에서 읽어오기 위한 Decoder입니다.
object RegionData {
    // `implicit` 키워드를 사용하여 `HeaderDecoder` 타입의 implicit 변수를 선언하였습니다. 이를 통해 필요한 경우 컴파일러가 자동으로 이 디코더를 찾아 사용할 수 있습니다.
    //
    // 이 변수는 `kantan.csv` 라이브러리가 CSV 파일을 `RegionData` 객체로 디코딩하는 데 사용됩니다.
    // 하지만 `implicit`를 사용했기 때문에 `RegionData` 객체를 직접 디코딩하는 코드를 작성하지 않아도 컴파일러가 알아서 적절한 디코딩 로직을 찾아 사용하게 됩니다.
    //
    // `implicit`은 변환기(converter), 매개변수 값 주입기(parameter value injector), 확장 메서드(extension method) 등의 역할을 할 수 있습니다.
    // 여기서는 매개변수 값 주입기의 역할을 하며, 컴파일러가 필요한 매개변수를 찾을 때 이 `implicit` 값을 사용하게 됩니다.
    //
    // ref: https://stackoverflow.com/questions/10375633/understanding-implicit-in-scala
    implicit val headerDecoder: HeaderDecoder[RegionData] = HeaderDecoder.decoder("격자 X", "격자 Y", "3단계")(RegionData.apply _)
}