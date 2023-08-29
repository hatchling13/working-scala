import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie.implicits._

object Delete {
  def deleteTableRow(name: String): ZIO[Database, Throwable, Int] = {
    val deleteQuery = tzio {
      sql"""|delete from test_table where name = ${name}""".stripMargin.update.run
    }

    val db = ZIO.service[Database]
    db.flatMap(database => database.transactionOrWiden(deleteQuery))
  }
}
