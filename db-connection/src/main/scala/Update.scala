import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie.implicits._

object Update {
  def updateTableRow(row: TestTableRow): ZIO[Database, Throwable, Int] = {
    val updateQuery = tzio {
      sql"""|update test_table set hobby = ${row.hobby} where name = ${row.name}""".stripMargin.update.run
    }

    val db = ZIO.service[Database]
    db.flatMap(database => database.transactionOrWiden(updateQuery))
  }
}
