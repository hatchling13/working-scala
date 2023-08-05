import doobie.enumerated.JdbcType
import java.util.Date
abstract class AbstractMood{
  def name:String
  def score:Int
}
case class Mood(name: String, score: Int) extends AbstractMood
case class DBMood(name: String, score: Int, createdAt: Date, id: Int) extends AbstractMood

object MoodParser {
    def parseInsertInput(score: String) =
        score match {
        case "10" => Mood("GOOD", 10)
        case "5" => Mood("SOSO", 5)
        case "0" => Mood("BAD", 0)
        case _ => Mood("NONE", -1)
  }
  
  def parseUpdateInput(id: String): Int =
    return id.toInt
}
