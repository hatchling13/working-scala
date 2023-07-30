package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class User(
    id: Long,
    // name: String,
    password: String,
    balance: Int,
)

object User {
    def createRentalRecord(record: RentalRecord): ZIO[Database, Throwable, Int] = {
        val insertQuery = tzio {
            sql"""
            |insert into rental_records (user_id, bike_id, start_station_id, time, cost)
            |values (${record.userId}, ${record.bikeId}, ${record.startStationId}, ${record.time}, ${record.cost})
            |""".stripMargin
            .update
            .run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(insertQuery))
    }

    def findUser(userId: Long): ZIO[Database, Throwable, Option[User]] = {
    val query = tzio {
        sql"""select id, password, balance from users where id = $userId""".query[User].option
    }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(query))
    }

    def deductBalance(userId: Long, amount: Int): ZIO[Database, Throwable, Unit] = {
            val updateQuery = tzio {
            sql"""update users set balance = balance - $amount where id = $userId""".update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(updateQuery)).unit
    }
}