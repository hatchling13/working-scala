// package com.bicycle_db

// import io.github.gaelrenoux.tranzactio.doobie.Database
// import io.github.gaelrenoux.tranzactio.ConnectionSource
// import zio.{Runtime, ZIO, ZLayer}
// import doobie._

// import io.github.gaelrenoux.tranzactio.ConnectionSource
// import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
// import zio._
// import doobie._
// import doobie.implicits._

// object ConnectDB extends ZIOAppDefault {
//     def insertUserInfo(user: User): ZIO[Database, Throwable, Int] = {
//         val insertQuery = tzio {
//             sql"""|insert into user (id, password, balance)
//                   |values (${user.id}, ${user.password}, ${user.balance})""".stripMargin.update.run
//         }

//         val db = ZIO.service[Database]
//         db.flatMap(database => database.transactionOrWiden(insertQuery))
//     }

//     val program = for {
//         _ <- ZIO.unit
//         database <- ZIO.service[Database]
//         _ <- insertUserInfo(User(1, "name", "password", 1000))
//         rows <- database
//             .transactionOrWiden(for {
//                 res <- tzio {
//                     sql"""|select id, password, balance
//                           |from user""".stripMargin
//                         .query[User]
//                         .to[List]
//                 }
//             } yield res)

//         _ <- zio.Console.printLine(rows)

//     } yield ()
// }

