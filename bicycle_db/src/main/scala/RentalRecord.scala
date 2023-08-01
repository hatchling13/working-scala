package com.bicycle_db

/*
```docker
CREATE TABLE rental_record (
    userId text NOT NULL,
    stationId integer,
    endStation integer,
    rentalTime integer,
    cost integer,
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (stationId) REFERENCES station(stationId),
    FOREIGN KEY (endStation) REFERENCES station(stationId)
);
*/
case class RentalRecord(userId: String, stationId: Int, endStation: Option[Int], rentalTime: Int, cost: Int)