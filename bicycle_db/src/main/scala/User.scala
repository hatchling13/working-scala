package com.bicycle_db

/*
```docker
CREATE TABLE users (
id text NOT NULL,
password text,
balance integer,
PRIMARY KEY (id)
);
*/
case class User(id: String, password: String, var balance: Int)