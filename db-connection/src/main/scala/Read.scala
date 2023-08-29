import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie.implicits._

object Read {
  def readTableRow() = {
     tzio {
      sql"""|select name, hobby
            |from test_table""".stripMargin
            .query[TestTableRow]
            .to[List]
    }
  }
}