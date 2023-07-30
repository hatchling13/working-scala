package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

case class Station(
    id: Long,
    name: String,
    availableBikes: Int,
)

object Station {
    def getAvailableBikes(stationId: Long): ZIO[Database, Throwable, Int] = {
        val query = tzio {
            sql"""
            |select available_bikes
            |from stations
            |where id = $stationId
            |""".stripMargin
            .query[Int]
            .unique
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(query))
    }

    def updateAvailableBikes(stationId: Long, change: Int): ZIO[Database, Throwable, Int] = {
        val updateQuery = tzio {
            sql"""
            |update stations
            |set available_bikes = available_bikes + $change
            |where id = $stationId
            |""".stripMargin
            .update
            .run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(updateQuery))
    }
}