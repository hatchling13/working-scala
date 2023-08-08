package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie._
import doobie.implicits._
import zio.ZIO
import bicycle_db.StationTableRow

case class Station(stationId: Int, var availableBikes: Int)

class StationServices(db: Database) {
  def insertStationTableRow(
      row: StationTableRow
  ): ZIO[Database, Throwable, Int] = {
    val insertStationTableQuery = tzio {
      (fr"insert into station (stationId, availableBikes) values (" ++ fr"${row.stationId}," ++ fr"${row.availableBicycles})").update.run
    }

    db.transactionOrWiden(insertStationTableQuery)
      .mapError(e => new Exception(e.getMessage))
  }

  def fetchAndPrintStationData(db: Database): ZIO[Any, Throwable, Unit] = for {
    stationInfo <- db.transactionOrWiden(for {
      res <- tzio {
        (fr"select stationId, availableBikes from station limit 10")
          .query[StationTableRow]
          .to[List]
      }
    } yield res)

    _ <- zio.Console.printLine(stationInfo)
  } yield ()

  def deleteAllStations: ZIO[Database, Throwable, Int] = {
    val deleteAllStationsQuery = tzio {
      (fr"delete from station").update.run
    }

    val db = ZIO.service[Database]
    db.flatMap(database =>
      database
        .transactionOrWiden(deleteAllStationsQuery)
        .mapError(e => new Exception(e.getMessage))
    )
  }
}
