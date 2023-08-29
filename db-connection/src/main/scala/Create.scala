import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie.implicits._

object Create {
  def insertTableRow(row: TestTableRow): ZIO[Database, Throwable, Int] = {
    val insertQuery = tzio {
      sql"""|insert into test_table (name, hobby)
            |values (${row.name}, ${row.hobby})""".stripMargin.update.run
    }

    val db = ZIO.service[Database]
    db.flatMap(database => database.transactionOrWiden(insertQuery))
  }
}