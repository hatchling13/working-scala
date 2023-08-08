import Mood._
import DBMood._
import doobie._
import doobie.implicits._

object DiaryRepository {
  def getAll() =
    // SELECT t.* FROM "DailyNotes".mood t
    sql"""SELECT t.* FROM "DailyNotes".mood t ORDER BY t."createdAt"""".stripMargin
      .query[DBMood]
      .to[List]

  def deleteOne(id: Int) =
    sql"""DELETE FROM "DailyNotes".mood WHERE id = ($id)""".update.run

  def updateOne(id: Int, targetMood: Mood) =
    sql"""UPDATE "DailyNotes".mood SET score = (${targetMood.score}), name = (${targetMood.name}) WHERE id = ($id)""".update.run

  def insertMood(name: String, score: Int) =
    sql"""INSERT INTO "DailyNotes".mood (name, score, "createdAt") VALUES ($name, $score, DEFAULT)""".update.run

  def deleteAllMood() =
    sql"""DELETE FROM "DailyNotes".mood """.update.run

}
