import Mood._
import doobie._
import doobie.implicits._

object SQLRepository {
  def getAll() =
    sql"""SELECT * from "DailyNotes".mood order by "createdAt"""".stripMargin.query[Mood].to[List]

  def deleteOne(id: Int) = 
    sql"""DELETE FROM "DailyNotes".mood WHERE id = ($id)""".update.run
  
  def updateOne(id: Int, score:Int) =
    sql"""UPDATE "DailyNotes".mood SET score = (${score}) WHERE id = ($id)"""
  
  
  def insertMood(name: String, score: Int) =
  sql"""insert into "DailyNotes".mood (name, score) values ($name, $score)""".update.run


  def deleteAllMood() =
  sql"""DELETE FROM "DailyNotes".mood """.update.run

}
