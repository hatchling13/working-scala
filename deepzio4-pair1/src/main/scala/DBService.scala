import zio._
import java.sql.{Connection, DriverManager, SQLException}

import doobie._
import doobie.implicits._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import io.github.gaelrenoux.tranzactio.ConnectionSource

case class Table(tableName: String)

case class User(id: Int, name: String, hp: Int)

case class CouponUser(name: String, level: Int)
case class Coupon(owner: String, discount: Int)

trait DBService {
  def connection: ZLayer[Any, SQLException, Connection]

  lazy val DBLayer =
    connection >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
}

object PostgreSQLService extends DBService {
  private val postgres = locally {
    val path = "localhost:5432"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  private val userTableName = "user_table"

  def connection = for {
    layer <- ZLayer(ZIO.attempt(DriverManager.getConnection(postgres)))
      .mapError(error => new SQLException(error))
  } yield layer

  def selectFromTable[A: Read](tableName: String, columns: List[String]) = for {
    db <- ZIO.service[Database]

    selectColumns <-
      if (columns.isEmpty) {
        ZIO.left("Please specify columns")
      } else {
        ZIO.right(Fragment.const(columns.mkString(", ")))
      }
    name = Fragment.const(tableName)

    result <- selectColumns match {
      case Left(error) => ZIO.left(error)
      case Right(columns) =>
        for {
          query <- db.transactionOrWiden(tzio {
            sql"select $columns from $name".query[A].to[List]
          })
          res <- query.isEmpty match {
            case true  => ZIO.left("Empty table")
            case false => ZIO.right(query)
          }
        } yield res
    }

  } yield result

  def insertCouponTable(coupons: List[Coupon]) = for {
    db <- ZIO.service[Database]

    sql = "insert into coupon_table (owner, discount) values (?, ?)"

    update = Update[Coupon](sql).updateMany(coupons)

    result <- db.transactionOrWiden(tzio { update })

  } yield result

  // ---

  def userTableExists(name: String) = for {
    db <- ZIO.service[Database]

    query = tzio {
      sql"""|select tablename from pg_tables where
            |schemaname = 'public'
            |""".stripMargin.query[Table].to[List]
    }

    result <- for {
      results <- db.transactionOrWiden(query)
      names = results.map(_.tableName)

      doesExists = names.exists(_ == userTableName) match {
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
          queryString = sql"""|create table ${Fragment.const(userTableName)} (
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
            sql"""|update ${Fragment.const(userTableName)}
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
