import zio._

import doobie._
import doobie.implicits._
import io.github.gaelrenoux.tranzactio.{DbException, doobie}
import io.github.gaelrenoux.tranzactio.doobie.{Database, TranzactIO, tzio}

case class Table(tableName: String)

case class TestTableRow(name: String, hobby: String)

class DBService {
  def tableExists(database: Database, tableName: String) = for {
    _ <- ZIO.unit

    query = tzio {
      sql"""|select tablename from pg_tables where
            |schemaname = 'public'
            |""".stripMargin.query[Table].to[List]
    }
    
    result <- for {
      results <- database.transactionOrWiden(query)
      name = results.map(_.tableName)

      doesExists = name.exists(_ == tableName) match {
        case false => Left("No table!")
        case true => Right(())
      }
    } yield doesExists
  } yield result

  def createTable(database:Database, tableName: String) = for {
    _ <- ZIO.unit

    exists <- this.tableExists(database, tableName)
    result <- exists match {
      case Left(_) =>
        for {
          _ <- ZIO.unit
          query = tzio {
            sql"""|create table ${Fragment.const(tableName)} (
                  |id serial primary key,
                  |name text not null,
                  |hobby text not null
                  |)
                  |""".stripMargin.update.run
          }
          _ <- database.transactionOrWiden(query)
          _ <- zio.Console.printLine("Table created!")
        } yield Left(())

      case Right(_) =>
          for {
            _ <- ZIO.unit
            _ <- zio.Console.printLine("Table exists.")
          } yield Right(())
    }
  } yield result

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
  def insertTableRow(database: Database, row: TestTableRow): ZIO[Any, DbException, Unit] = for {
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
  def updateTableRow(database: Database, row: TestTableRow): ZIO[Any, DbException, Unit] = for {
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
  def deleteTableRow(database: Database, name: String): ZIO[Any, DbException, Unit] = for {
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