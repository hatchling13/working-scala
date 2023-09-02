import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO, tzio}
import zio._
import doobie.implicits._
import io.github.gaelrenoux.tranzactio.{DbException, doobie}


case class TestTableRow(name: String, hobby: String)

class DBService {
  def selectTableRow(database: Database) = for {
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select name, hobby
                |from Person""".stripMargin
            .query[TestTableRow]
            .to[List]
        }
      } yield res)
  } yield (rows)


  // INSERT Method
  def insertTableRow(database: doobie.Database.Service, row: TestTableRow): ZIO[Any, DbException, Unit] = for {
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|insert into Person (name, hobby)
                |values (${row.name}, ${row.hobby})"""
            .stripMargin
            .update
            .run
        }
      } yield res)
  } yield ()


  // UPDATE Method
  def updateTableRow(database: doobie.Database.Service, row: TestTableRow): ZIO[Any, DbException, Unit] = for {
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|update Person set hobby = ${row.hobby} where name = ${row.name}"""
            .stripMargin
            .update
            .run
        }
      } yield res)
  } yield ()


  // DELETE Method
  def deleteTableRow(database: doobie.Database.Service, name: String): ZIO[Any, DbException, Unit] = for {
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|delete from Person where name = ${name}"""
            .stripMargin
            .update
            .run
        }
      } yield res)
  } yield ()
}