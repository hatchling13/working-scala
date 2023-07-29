import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class TestTableRow(name: String, hobby: String)

object DBSampleApp extends ZIOAppDefault {

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select name, hobby
                |from test_table""".stripMargin
            .query[TestTableRow]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

  } yield ()
  override def run = prog.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

  val sqlite = locally {
    val path = "identifier.sqlite"
    s"jdbc:sqlite:$path"
  }
  val postgres = locally {
    val path = "localhost:5432"
    val name = "postgres"
    val user = "postgres"
    val password = "1234"
    s"jdbc:postgresql://$path/$name?user$user=&password=$password"
  }

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}
