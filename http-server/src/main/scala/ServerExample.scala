import zio._
import zio.http.{ZClient, _}

import java.io.IOException

object ServerExample extends ZIOAppDefault {

  val app =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "text" =>
        for {
          _ <- zio.Console.printLine("/text endpoint!")
          res <- ZIO.succeed(Response.text("Hello World!"))
        } yield res
      case Method.GET -> Root / "apple" =>
        for {
          _ <- zio.Console.printLine("/apple endpoint!")
          res <- ZIO.succeed(Response.text("APPLE!"))
        } yield res
      case Method.GET -> Root =>
        for {
          _ <- zio.Console.printLine("root endpoint!")
          url = URL.decode("http://localhost:13333/apple").toOption.get
          res <- ZClient.request(Request.get(url))
        } yield res

      case req @ Method.POST -> Root / "client-test" =>
        for {
          _ <- zio.Console.printLine("client-test")
          _ <- zio.Console.printLine(req.body)
          res <- ZIO.succeed(Response.text("HELLO!"))
//          res <- ZIO.succeed(Response.text(
//            """
//              |{ "count" : -12}
//              |""".stripMargin))
        } yield (res)
    }

  override val run =
    Server
      .serve(app.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
  abstract class Notification

  case class Email(sourceEmail: String, title: String, body: String)
      extends Notification

  case class SMS(sourceNumber: String, message: String) extends Notification

  case class VoiceRecording(contactName: String, link: String)
      extends Notification

  case class Board(title: String, txt: String, notification: List[Notification])

  //    _ <- zio.Console.printLine("")
  //    b = Board(
  //      "a",
  //      "title",
  //      List(
  //        Email("a", "hello english man ", "c"),
  //        SMS("010-111203391", "아무내용이ㅣ"),
  //        SMS("김사장", "주말출근 가능하신분?")
  //      )
  //    )
  //    _ = println(b)
  //    _ = b.notification.foreach { noti =>
  //      val a = noti match {
  //        case Email(sourceEmail, title, body) =>
  //          s"이 이메일은 영국에서 시작되었으며 제목은 $title 인데....?"
  //        case SMS(sourceNumber, message) if sourceNumber == "김사장" => "차단"
  //        case SMS(sourceNumber, message) =>
  //          s"국제번호에서 온 SMS 입니다 $sourceNumber, $message"
  //        case VoiceRecording(contactName, link) =>
  //          s"아쉽지만 text로는 들려줄 수 없네요 $contactName 직접 전화 거세요"
  //        case _ => "뭔지 모르겠는데요?"
  //      }
  //      println(a)
  //
  //    }
  //  } yield ()

}

// 배울 것
// case class ADT <- 데이터 모델링
// 아키텍쳐, 의존성 주입
// http git

// 안 배울 것
// implicit
// macro
// 특이한 스칼라 문법
// 모나드
//
