import sttp.client3.{HttpClientSyncBackend, UriContext, basicRequest}
import sttp.client3.ziojson._
import zio.json._

object CookingInfo extends App {
  private val backend = HttpClientSyncBackend()

  private val dish1 = Dish("초밥", List("밥", "생선회", "식초", "와사비"), Japanese())
  private val dish2 = Dish("김치찌개", List("김치", "돼지고기", "물", "간장", "참기름", "파", "양파"), Korean())
  private val dish3 = Dish("짜장면", List("춘장", "돼지고기", "양파", "양배추", "면"), Chinese())

  private val dishList = List(dish1, dish2, dish3)

  private val response = basicRequest.post(uri"http://localhost:13333/client-test").body(dishList).send(backend)

  response.body match {
    case Left(error) => println(s"Error: $error")
    case Right(value) => println(value)
  }
}

case class Dish(name: String, ingredients: List[String], country: Country)
object Dish {
  implicit val decoder: JsonDecoder[Dish] = DeriveJsonDecoder.gen[Dish]
  implicit val encoder: JsonEncoder[Dish] = DeriveJsonEncoder.gen[Dish]
}

sealed trait Country
object Country {
  implicit val decoder: JsonDecoder[Country] = DeriveJsonDecoder.gen[Country]
  implicit val encoder: JsonEncoder[Country] = DeriveJsonEncoder.gen[Country]
}
case class Korean() extends Country
case class Japanese() extends Country
case class Chinese() extends Country
