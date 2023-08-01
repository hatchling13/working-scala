// Business Logic for Bicycle Rental Service
package com.bicycle_db

import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._
import cats.implicits._

class BicycleRentalService(db: Database.Service) {

    def rentBicycle(userId: String, stationId: Int, rentalTime: Int): ZIO[Any, Throwable, Int] = {
        val rentalRecord = RentalRecord(userId, stationId, None, rentalTime, 1000)
        val rentBicycleQuery = tzio {
            sql"""|UPDATE users
                  |SET balance = balance - ${rentalRecord.cost}
                  |WHERE userId = ${rentalRecord.userId}""".stripMargin.update.run *> 
            sql"""|INSERT INTO rental_record (user_id, station_id, rental_time, cost)
                  |VALUES (${rentalRecord.userId}, ${rentalRecord.stationId}, ${rentalRecord.rentalTime}, ${rentalRecord.cost})""".stripMargin.update.run *>
            sql"""|UPDATE station
                  |SET available_bicycles = available_bicycles - 1
                  |WHERE stationId = ${rentalRecord.stationId}""".stripMargin.update.run
        }

        db.transactionOrWiden(rentBicycleQuery)
    }
}