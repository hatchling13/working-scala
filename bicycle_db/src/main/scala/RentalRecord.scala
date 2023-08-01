package com.bicycle_db

import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie._
import doobie.implicits._
import bicycle_db.RentalRecordRow
import zio.ZIO

case class RentalRecord(userId: String, stationId: Int, endStation: Option[Int], rentalTime: Int, cost: Int)

class RentalRecordServices(db: Database) {
    def insertRentalRecordRow(row: RentalRecordRow): ZIO[Database, Throwable, Int] = {
        val insertRentalRecordQuery = tzio {
            (fr"insert into rental_record (userId, stationId, endStation, rentalTime, cost) values (" ++ fr"${row.userId}," ++ fr"${row.stationId}," ++ fr"${row.endStation}," ++ fr"${row.rentalTime}," ++ fr"${row.cost})").update.run
        }

        db.transactionOrWiden(insertRentalRecordQuery)
    }

    def fetchAndPrintRentalRecordData(db: Database): ZIO[Any, Throwable, Unit] = for {
        rentalRecordInfo <- db.transactionOrWiden(for {
            res <- tzio {
                (fr"select userId, stationId, endStation, rentalTime, cost from rental_record limit 10").query[RentalRecordRow].to[List]
            }
        } yield res)

        _ <- zio.Console.printLine(rentalRecordInfo)
    } yield ()

    def deleteAllRentalRecords: ZIO[Database, Throwable, Int] = {
        val deleteAllRentalRecordsQuery = tzio {
            (fr"delete from rental_record").update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(deleteAllRentalRecordsQuery))
    }
}