import java.util.Date
import zio._
import doobie.enumerated.JdbcType
import DiaryApp._
abstract class AbstractMood {
  def name: String
  def score: Int
}
case class Mood(name: String, score: Int) extends AbstractMood
// 중간에 테이블을 수정해서 추가된 DBMood...
case class DBMood(name: String, score: Int, createdAt: Date, id: Int)
    extends AbstractMood

trait Error
case class InvalidInputError(message: String)
    extends RuntimeException
    with Error

object MoodParser {
  def parseInsertInput(score: String) =
    score match {
      case "10" => ZIO.succeed(Mood("GOOD", 10))
      case "5"  => ZIO.succeed(Mood("SOSO", 5))
      case "0"  => ZIO.succeed(Mood("BAD", 0))
      case _    => ZIO.fail(InvalidInputError("잘못 입력하셨어요"))
    }

  def parseUpdateInput(id: String): Int =
    return id.toInt
}
