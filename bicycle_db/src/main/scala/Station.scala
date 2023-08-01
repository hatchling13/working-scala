package com.bicycle_db

/*
```docker
CREATE TABLE station (
stationId integer NOT NULL,
availableBikes integer,
PRIMARY KEY (stationId)
);
*/
case class Station(stationId: Int, var availableBikes: Int)