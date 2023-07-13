import zio._

// 추상 클래스 만들고
abstract class Notification
// 케이스 클래스 만들면
case class Email(sourceEmail: String, title: String, body: String) extends Notification
case class SMS(sourceNumber: String, message: String) extends Notification
case class VoiceRecording(contactName: String, link: String) extends Notification
// Notification을 List로 받았을 때 각각을 생성해서 Board를 만들 수 있음
case class Board(notifications: List[Notification])

object Main extends ZIOAppDefault {
  override def run =
    for {
      _ <- ZIO.unit
      b = Board(List(Email("aa@gmail.com", "이메일제목임", "내용임"), SMS("김사장", "메롱"), SMS("01012345678", "내용임"), VoiceRecording("김사장", "출근해")))
      _ = println(b)
      _ = b.notifications.foreach{noti =>
        // 패턴 매칭 실습
        val a = noti match {
          case Email(sourceEmail, title, body) => s"이 이메일은 영국에서 시작되었으며 ... $title"
          case SMS(sourceNumber, message) if sourceNumber == "김사장" => "차단"  // 패턴 가드
          case SMS(sourceNumber, message) => s"국제번호에서 온 SMS입니다."
          case VoiceRecording(contactName, link) => s"아쉽지만 text로 들려줄 수 없어 $contactName 으로 전화하세요"
          case _ => "뭔지 모르겠어요" // 매칭이 안되면 _로 빠진다.
        }
        println(a)
      }
    } yield ()
}
// case class ADT 데이터 모델링
// 아키텍처, 의존성 주입
// http, git

// implicit, 매크로, 특이한 스칼라 문법, 모나드 , 함수형 심취 등등 안함
