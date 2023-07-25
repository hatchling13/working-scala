import zio._
import sttp.client3._

object ClientExample extends App {

  //https://sttp.softwaremill.com/en/stable/quickstart.html
  val backend = HttpClientSyncBackend()
  val response = basicRequest
    .body("Hello, world!")
    .post(uri"http://localhost:13333/client-test")
    .send(backend)

  println(response.body)

  //https://sttp.softwaremill.com/en/stable/json.html
//  import sttp.client3._
//  import sttp.client3.ziojson._
//  import zio.json._
//
//  val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
//
//  implicit val payloadJsonEncoder: JsonEncoder[RequestPayload] =
//    DeriveJsonEncoder.gen[RequestPayload]
//  implicit val myResponseJsonDecoder: JsonDecoder[ResponsePayload] =
//    DeriveJsonDecoder.gen[ResponsePayload]
//
//  val requestPayload = RequestPayload("some data")
//
//  val response: Identity[
//    Response[Either[ResponseException[String, String], ResponsePayload]]
//  ] =
//    basicRequest
//      .post(uri"http://localhost:13333/client-test")
//      .body(requestPayload)
//      .response(asJson[ResponsePayload])
//      .send(backend)
//
//  case class RequestPayload(msg: String)
//
//  case class ResponsePayload(count: Int)
//
//  val run = ZIO.attempt(response).debug("res")

}
