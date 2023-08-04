package server

import zio._
import zio.http._
import zio.json._

object ServerExample extends ZIOAppDefault {

  val app =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "reporting-test" =>
        for {
          age <- Random.nextIntBounded(13)
          hobbies = List("공차기", "요리하기", "스쿠터", "코딩")
          shuffledHobbies <- Random.shuffle(hobbies)
          firstHobbies = shuffledHobbies.take(2)
          friend: Friend = Friend("익명", age, firstHobbies, "비밀의장소")
          _ <- ZIO.sleep(2.second)
          res <- ZIO.succeed(Response.text(friend.toJson))
          _ <- zio.Console.printLine(s"2초 쉬고 보냈습니다 >>> $friend")
        } yield res
    }

  case class Friend(name: String, age: Int, hobbies: List[String], location: String)

  object Friend {
    implicit val decoder: JsonDecoder[Friend] = DeriveJsonDecoder.gen[Friend]
    implicit val encoder: JsonEncoder[Friend] = DeriveJsonEncoder.gen[Friend]
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
