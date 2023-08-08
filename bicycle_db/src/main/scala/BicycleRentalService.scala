// Business Logic for Bicycle Rental Service
package com.bicycle_db

import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._
import cats.implicits._

class BicycleRentalService(db: Database) {

  case class DatabaseError(message: String) extends Exception(message)

  def fromSqlException: PartialFunction[Throwable, DatabaseError] = {
    case e: java.sql.SQLException => DatabaseError(e.getMessage)
  }

  def rentBike(
      userId: String,
      stationId: Int,
      rentalTime: Int
  ): ZIO[Any, Throwable, Int] = {
    val rentalRecord = RentalRecord(userId, stationId, None, rentalTime, 1000)
    // to prevent SQL injection, use doobie's Fragment API(`fr`) instead of string interpolation
    val rentBicycleQuery = tzio {
      (fr"UPDATE users SET balance = balance -" ++ fr"${rentalRecord.cost}" ++ fr"WHERE id =" ++ fr"$userId").update.run *>
        (fr"INSERT INTO rentalRecord (userId, stationId, rentalTime, cost) VALUES (" ++ fr"$userId," ++ fr"$stationId," ++ fr"${rentalRecord.rentalTime}," ++ fr"${rentalRecord.cost}").update.run *>
        (fr"UPDATE station SET availableBicycles = availableBikes - 1 WHERE stationId =" ++ fr"$stationId").update.run
    }

    db.transactionOrWiden(rentBicycleQuery).mapError(fromSqlException)
  }

  def returnBike(
      userId: String,
      returnStationId: Int
  ): ZIO[Any, Throwable, Int] = {
    val returnBikeQuery = tzio {
      (fr"UPDATE rentalRecord SET endStation =" ++ fr"$returnStationId" ++ fr"WHERE userId =" ++ fr"$userId" ++ fr"AND endStation IS NULL").update.run *>
        (fr"UPDATE station SET availableBikes = availableBikes + 1 WHERE stationId =" ++ fr"$returnStationId").update.run
    }

    db.transactionOrWiden(returnBikeQuery).mapError(fromSqlException)
  }

  def checkBikeAvailability(stationId: String): ZIO[Any, Throwable, Boolean] = {
    val checkBikeAvailabilityQuery = tzio {
      (fr"SELECT EXISTS (SELECT * FROM station WHERE stationId =" ++ fr"$stationId" ++ fr"AND availableBikes > 0)")
        .query[Boolean]
        .unique
    }

    db.transactionOrWiden(checkBikeAvailabilityQuery).mapError(fromSqlException)
  }

  def calculateRentalCost(rentalTime: Int): Int = {
    rentalTime * 1000
  }

  //// Login System ////

  def verifyUser(
      userId: String,
      password: String
  ): ZIO[Any, Throwable, Boolean] = {
    val verifyUserQuery = tzio {
      (fr"SELECT EXISTS (SELECT * FROM users WHERE id =" ++ fr"$userId" ++ fr"AND password =" ++ fr"$password)")
        .query[Boolean]
        .unique
    }

    db.transactionOrWiden(verifyUserQuery).mapError(fromSqlException)
  }
}
