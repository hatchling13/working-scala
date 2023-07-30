package com.bicycle_db

import zio._ 
import io.github.gaelrenoux.tranzactio.doobie.Database

class BikeRentalService(database: Database.Service) {

    import com.bicycle_db.User
    import com.bicycle_db.RentalRecord
    import com.bicycle_db.Station

    def rentBike(userId: Long, password: String, bikeId: Long, startStationId: Long, time: Int): ZIO[Database, Throwable, RentalRecord] = {
        for {
            user <- User.findUser(userId).someOrFail(new Exception("User not found"))
            _ <- ZIO.when(user.password != password)(ZIO.fail(new Exception("Wrong password")))
            _ <- ZIO.when(user.balance < 0)(ZIO.fail(new Exception("Not enough money")))
            _ <- User.deductBalance(userId, 1)
            _ <- Station.updateAvailableBikes(startStationId, -1)
            record = RentalRecord(userId, bikeId, time, startStationId, -1, 1)
        } yield record
    }
}

object BikeRentalService {
    def apply(database: Database.Service): BikeRentalService = new BikeRentalService(database)
}