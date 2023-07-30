package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class RentalRecord(
    userId: Long,
    bikeId: Long,
    time: Int,
    startStationId: Long,
    endStationId: Long, // 반소 정류소
    cost: Int,
)

object RentalRecord {
    def createRentalRecord(record: RentalRecord): ZIO[Database, Throwable, Long] = {
        val insertQuery = tzio {
            sql"""
            |insert into rental_records (user_id, bike_id, start_station_id, time, cost)
            |values (${record.userId}, ${record.bikeId}, ${record.startStationId}, ${record.time}, ${record.cost})
            |""".stripMargin
            .update
            .withUniqueGeneratedKeys[Long]("id")
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(insertQuery))
    }

    def finishRentalBike(recordId: Long, endStationId: Long): ZIO[Database, Throwable, Int] = {
        val updateQuery = tzio {
            sql"""
            |update rental_records
            |set end_station_id = $endStationId
            |where id = $recordId
            |""".stripMargin
            .update
            .run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(updateQuery))
    }
}