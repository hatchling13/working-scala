package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie._
import doobie.implicits._
import bicycle_db.UsersTableRow
import zio.ZIO

case class User(id: String, password: String, var balance: Int)

class UserServices(db: Database) {
    def insertUserTableRow(row: UsersTableRow): ZIO[Database, Throwable, Int] = {
        val insertUserTableQuery = tzio {
            (fr"insert into users (id, password, balance) values (" ++ fr"${row.userId}," ++ fr"${row.password}," ++ fr"${row.balance})").update.run
        }

        db.transactionOrWiden(insertUserTableQuery).mapError(e => new Exception(e.getMessage))
    }

    def fetchAndPrintUserData(db: Database): ZIO[Any, Throwable, Unit] = for {
        userInfo <- db.transactionOrWiden(for {
            res <- tzio {
                (fr"select id, password, balance from users limit 10").query[UsersTableRow].to[List]
            }
        } yield res)
        _ <- zio.Console.printLine(userInfo)
    } yield ()

    def deleteAllUsers: ZIO[Database, Throwable, Int] = {
        val deleteAllUsersQuery = tzio {
            (fr"delete from users").update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(deleteAllUsersQuery).mapError(e => new Exception(e.getMessage)))
    }
}