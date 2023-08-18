import zio._
import java.sql.{Connection, DriverManager, SQLException}

import doobie._
import doobie.implicits._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}

case class User(id: Int, name: String, hp: Int)
case class Table(tableName: String)

trait DBConnection {
  def connection: ZLayer[Any, SQLException, Connection]
}

object PostgreSQLConnection extends DBConnection {
  private val postgres = locally {
    val path = "localhost:5432"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  private val tableName = "user_table"

  def connection = for {
    layer <- ZLayer(ZIO.attempt(DriverManager.getConnection(postgres)))
      .mapError(error => new SQLException(error))
  } yield layer

  def userTableExists() = for {
    db <- ZIO.service[Database]

    query = tzio {
      sql"""|select tablename from pg_tables where
            |schemaname = 'public'
            |""".stripMargin.query[Table].to[List]
    }

    result <- for {
      results <- db.transactionOrWiden(query)
      names = results.map(_.tableName)

      doesExists = names.exists(_ == tableName) match {
        case false => Left("No table!")
        case true  => Right(())
      }
    } yield doesExists
  } yield result

  def createUserTable(doesTableExists: Either[String, Unit]) = for {
    db <- ZIO.service[Database]

    result <- doesTableExists match {
      case Left(_) =>
        for {
          _ <- ZIO.unit
          queryString = sql"""|create table ${Fragment.const(tableName)} (
                              |id serial primary key,
                              |name text not null,
                              |hp integer not null
                              |)
                              |""".stripMargin

          action = tzio { queryString.update.run }

          _ <- db.transactionOrWiden(action)
        } yield Left("Table created!")
      case Right(_) =>
        for {
          _ <- ZIO.unit
        } yield Right(())
    }
  } yield result

  def getUser(id: Int) = for {
    db <- ZIO.service[Database]

    queryString = sql"""|select * from ${Fragment.const(tableName)}
                  |where id = ${Fragment.const(id.toString())}""".stripMargin

    action = tzio { queryString.query[User].to[List] }

    result <- db.transactionOrWiden(action)

    user = result.isEmpty match {
      case true  => Left("No such user")
      case false => Right(result.head)
    }
  } yield user

  def updateUser(nextUser: Either[String, User]) = for {
    db <- ZIO.service[Database]

    result <- nextUser match {
      case Left(errorMessage) =>
        for {
          _ <- ZIO.unit
        } yield Left(errorMessage)
      case Right(user) =>
        for {
          _ <- ZIO.unit
          queryString = {
            sql"""|update ${Fragment.const(tableName)}
                  |set
                  |hp = ${user.hp}
                  |where id = ${user.id}
                  |""".stripMargin
          }

          action = tzio {
            queryString.update.run
          }

          _ <- db.transactionOrWiden(action)
        } yield Right(s"HP of ${user.name} is ${user.hp}!")
    }
  } yield result
}
