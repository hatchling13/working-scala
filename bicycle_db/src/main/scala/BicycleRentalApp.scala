package bicycle_db

import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._
import java.sql.DriverManager

// Placeholders for the tables in the database
case class UsersTableRow(userId: String, password: String, balance: Int)
case class StationTableRow(stationId: Int, var availableBicycles: Int)
case class RentalRecordRow(userId: String, stationId: Int, endStation: Option[Int], rentalTime: Int, cost: Int)


// ref: https://judo0179.tistory.com/96
object BicycleRentalApp extends ZIOAppDefault {

    def insertUserTableRow(row: UsersTableRow): ZIO[Database, Throwable, Int] = {
        val insertUserTableQuery = tzio {
            sql"""|insert into users (id, password, balance)
                  |values (${row.userId}, ${row.password}, ${row.balance})""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(insertUserTableQuery))
    }

    def insertStationTableRow(row: StationTableRow): ZIO[Database, Throwable, Int] = {
        val insertStationTableQuery = tzio {
            sql"""|insert into station (stationId, availableBikes)
                  |values (${row.stationId}, ${row.availableBicycles})""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(insertStationTableQuery))
    }

    def insertRentalRecordRow(row: RentalRecordRow): ZIO[Database, Throwable, Int] = {
        val insertRentalRecordQuery = tzio {
            sql"""|insert into rental_record (userId, stationId, endStation, rentalTime, cost)
                  |values (${row.userId}, ${row.stationId}, ${row.endStation}, ${row.rentalTime}, ${row.cost})""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(insertRentalRecordQuery))
    }

    def deleteAllUsers: ZIO[Database, Throwable, Int] = {
        val deleteAllUsersQuery = tzio {
            sql"""|delete from users""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(deleteAllUsersQuery))
    }

    def deleteAllStations: ZIO[Database, Throwable, Int] = {
        val deleteAllStationsQuery = tzio {
            sql"""|delete from station""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(deleteAllStationsQuery))
    }

    def deleteAllRentalRecords: ZIO[Database, Throwable, Int] = {
        val deleteAllRentalRecordsQuery = tzio {
            sql"""|delete from rental_record""".stripMargin.update.run
        }

        val db = ZIO.service[Database]
        db.flatMap(database => database.transactionOrWiden(deleteAllRentalRecordsQuery))
    }

    val prog = for {
        _ <- ZIO.unit
        database <- ZIO.service[Database]
        // insert some data into the database
        _ <- insertUserTableRow(UsersTableRow("foobrrrrra", "password1", 1000))
        _ <- insertStationTableRow(StationTableRow(160, 10)) // start station
        _ <- insertStationTableRow(StationTableRow(668, 10)) // end station
        // get all user rows from the database and print them
        userRows <- database
            .transactionOrWiden(for {
                res <- tzio {
                    sql"""|select id, password, balance
                          |from users
                          |limit 10""".stripMargin
                        .query[UsersTableRow]
                        .to[List]
                }
            } yield res)
        _ <- zio.Console.printLine(userRows)
        // get all station rows from the database and print them
        stationRows <- database
            .transactionOrWiden(for {
                res <- tzio {
                    sql"""|select stationId, availableBikes
                          |from station
                          |limit 10""".stripMargin
                        .query[StationTableRow]
                        .to[List]
                }
            } yield res)
            _ <- zio.Console.printLine(stationRows)

        // get all rental record rows from the database and print them
        rentalRecordRows <- database
            .transactionOrWiden(for {
                res <- tzio {
                    sql"""|select userId, stationId, endStation, rentalTime, cost
                          |from rental_record
                          |limit 10""".stripMargin
                        .query[RentalRecordRow]
                        .to[List]
                }
            } yield res)
            _ <- zio.Console.printLine(rentalRecordRows)
            // for testing purposes, delete all rows from the database
            // _ <- deleteAllUsers
            // _ <- deleteAllStations
            // _ <- deleteAllRentalRecords
    } yield ()

    override def run = prog.provide(
        conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
    )

    // TODO should not be hardcoded
    val postgres = locally {
        val path = "localhost:5432"
        val name = "rental_service"
        val user = "notjoon"
        val password = "1q2w3e4r"

        s"jdbc:postgresql://$path/$name?user=$user&password=$password"
    }

    private val conn = ZLayer(
        ZIO.attempt(
            java.sql.DriverManager.getConnection(
                postgres
            )
        )
    )
}

// docker run -p 5400:5400 --name bicycle -e POSTGRES_PASSWORD=1q2w3e4r -d postgres

