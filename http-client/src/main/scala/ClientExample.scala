import sttp.client3.ziojson.asJson
import zio._
import sttp.client3._
import zio.json.{
  DeriveJsonDecoder,
  DeriveJsonEncoder,
  EncoderOps,
  JsonDecoder,
  JsonEncoder
}

case class Friend(
    name: String,
    age: Int,
    hobbies: List[String],
    location: String
)

object Friend {
  implicit val decoder: JsonDecoder[Friend] = DeriveJsonDecoder.gen[Friend]
  implicit val encoder: JsonEncoder[Friend] = DeriveJsonEncoder.gen[Friend]
}

object Reporting extends ZIOAppDefault {
  val prog = for {
    _ <- ZIO.unit
    backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    response = basicRequest
      .get(uri"http://localhost:13333/reporting-test")
      .response(asJson[Friend])
      .send(backend)
    f <- response.body match {
      case Left(_) => ZIO.fail(new Exception("fail"))
      case Right(friend) => {
        ZIO.succeed(friend)
      }
    }
  } yield f
  override def run = for {
    friends <- ZIO.foreach(1 to 100) { _ =>
      prog.debug("zz")
    }
    _ = println(friends.toJson)
    ageSum = friends.map(_.age).sum
    _ <- zio.Console.printLine(ageSum)
  } yield ()

}
