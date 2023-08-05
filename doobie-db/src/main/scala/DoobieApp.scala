import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie.implicits._
import doobie._
import zio._
import zio.{Console}
import DiaryService._
import PostgresConnection.conn

// 하나 삭제
// 하나 수정
// 끝내기?
case class Action(number:String)
object DoobieApp extends ZIOAppDefault {
  val selectAction =
    for{
    _ <- Console.printLine(
      """
      원하는 행동을 번호로 입력해주세요.
      _________________________________
      [1] 오늘의 기분 입력하기
      [2] 기록한 기분 수정하기
      [3] 기록한 기분 삭제하기
      [4] 지금까지 기록한 기분 보기
      """)
      input <- Console.readLine("번호로 입력 :")
      action <- input match {
        case "1" => addTodayMood
        case "2" => modifyMood
        case "3" => deleteMood
        case "4" => getAllMoods
        case _ => ZIO.fail("잘못 입력하셨습니다.")
      } 

  } yield(action)

  val main = selectAction
  

  override def run = main.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

}