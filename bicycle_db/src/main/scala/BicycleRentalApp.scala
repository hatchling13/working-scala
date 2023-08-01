package bicycle_db

import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._

import com.bicycle_db.BicycleRentalService
import com.bicycle_db.UserServices
import com.bicycle_db.StationServices
import com.bicycle_db.RentalRecordServices

// Placeholders for the tables in the database
case class UsersTableRow(userId: String, password: String, balance: Int)
case class StationTableRow(stationId: Int, var availableBicycles: Int)
case class RentalRecordRow(userId: String, stationId: Int, endStation: Option[Int], rentalTime: Int, cost: Int)


// ref: https://judo0179.tistory.com/96
object BicycleRentalApp extends ZIOAppDefault {

    val prog = for {
        _ <- ZIO.unit
        database <- ZIO.service[Database]

        // create services instances
        userService = new UserServices(database)
        stationService = new StationServices(database)
        rentalRecordService = new RentalRecordServices(database)

        // for testing purposes, delete all rows from the database
        // _ <- userService.deleteAllUsers
        // _ <- stationService.deleteAllStations
        // _ <- rentalRecordService.deleteAllRentalRecords

        rentalService = new BicycleRentalService(database)

        //insert some data into the database
        // _ <- userService.insertUserTableRow(UsersTableRow("foobar", "password1", 1000))
        // _ <- stationService.insertStationTableRow(StationTableRow(123, 10)) // start station
        // _ <- rentalRecordService.insertStationTableRow(StationTableRow(456, 10)) // end station

        // login system
        _ <- Console.printLine("Enter your user id: ")
        userId <- Console.readLine
        _ <- Console.printLine("Enter your password: ")
        password <- Console.readLine

        // check if the user is verified or not. if not, fail the program
        isVerified <- rentalService.verifyUser(userId, password)
        _ <- if (isVerified) 
                Console.printLine("Log in") 
            else 
                ZIO.fail("Can't find user")

        // rent a bicycle
        _ <- Console.printLine("Enter the station id: ")
        stationId <- Console.readLine
        isAvailable <- rentalService.checkBikeAvailability(stationId)
        // if `isAvailable`, then get `rentTime` and proceed to rent a bicycle
        // else, fail the program
        _ <- if (isAvailable) {
                for {
                    _ <- Console.printLine("Enter the rental time: ")
                    rentalTime <- Console.readLine
                    rentalCost = rentalService.calculateRentalCost(rentalTime.toInt)
                    _ <- rentalService.rentBike(userId, stationId.toInt, rentalTime.toInt)
                    _ <- Console.printLine(s"Your rental cost is $rentalCost")
                } yield ()
            } else {
                ZIO.fail("No available bikes")
            }
        
        // return a bicycle
        _ <- Console.printLine("Enter the station id: ")
        returnStationId <- Console.readLine
        _ <- rentalService.returnBike(userId, returnStationId.toInt)
        _ <- Console.printLine("Bike has returned. Thank you for using our service!")
    } yield ()

    override def run = prog.provide(
        conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
    )

    // docker run -p 5400:5400 --name bicycle -e POSTGRES_PASSWORD=<password> -d postgres
    val postgres = locally {
        val path = "localhost:5432"
        val name = "rental_service"
        val user = ???
        val password = ???

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

