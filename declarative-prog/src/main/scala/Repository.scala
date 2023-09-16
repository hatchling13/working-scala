import zio._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import io.github.gaelrenoux.tranzactio.{ConnectionSource, DbException, doobie}
import _root_.doobie.implicits._
import _root_.doobie._
import Util._

object Repository {

  def findRestaurantById(id: Int) =
    for {
      db <- ZIO.service[Database.Service]
      restaurant <- db
        .transactionOrWiden(for {
          res <- tzio {
            sql"""|select * from restaurant where id = ${id}""".stripMargin
              .query[Restaurant]
              .unique
          }

        } yield (res))
    } yield (restaurant)

  def getReservationByInfo(info: UserInfo) =
    for {
      db <- ZIO.service[Database.Service]
      reservation <- db
        .transactionOrWiden(for {
          res <- tzio {
            sql"""|select * from reservation where name = ${info.name} and phone = ${info.phone}""".stripMargin
              .query[Reservation]
              .unique
              // .to[List]
              
          }
        } yield (res))
    } yield (reservation)

  def getReservationByNumber(number: Int) = 
    for {
    db <- ZIO.service[Database.Service]
      reservation <- db
        .transactionOrWiden(for {
          res <- tzio {
            sql"""|select * from reservation where reservation_id = ${number}""".stripMargin
              .query[Reservation]
              .unique
          }
        } yield (res))
    } yield (reservation)

  def makeReservationToRestaurant(restaurant: Restaurant, reservation: Reservation) = for {
    db <- ZIO.service[Database.Service]
    insertResult <- db
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|INSERT INTO reservation (name, phone, restaurant_id, reservation_date, reservation_time, guests) VALUES
                    |(${reservation.userInfo.name},
                    | ${reservation.userInfo.phone},
                    | ${restaurant.id},
                    | ${reservation.reservation_date},
                    | ${reservation.reservation_time},
                    | ${reservation.guests}
                    | ${None})""".stripMargin
            .update()
            .run
        }
      } yield ())
  } yield ()

  def saveCoupon(coupon: Coupon) = for {
    db <- ZIO.service[Database.Service]
    _ <- db
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|insert into coupon values
                |(${coupon.owner},
                |${coupon.discount_rate})""".stripMargin
            .update()
            .run
        }
      } yield ())
  } yield ()

  def getAllRestaurantList()
      : ZIO[doobie.Database.Service, Exception, List[Restaurant]] = 
    for {
    database <- ZIO.service[Database.Service]
    // 예약 가능한 전체 식당 조회해서 출력
    list <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select id, name
                |from restaurant""".stripMargin
            .query[Restaurant]
            .to[List]
        }
      } yield res)

  } yield list

  def checkIfRestaurantExist(
      restaurantId: String
  ): ZIO[doobie.Database.Service, DbException, Boolean] = for {
    database <- ZIO.service[Database.Service]
    result <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select EXISTS(select * from restaurant where id = ${Integer
            .parseInt(restaurantId)})""".stripMargin
            .query[Boolean]
            .unique
        }
      } yield res)
  } yield result

  def checkIfReservationExist(
      info: UserInfo
  ): ZIO[doobie.Database.Service, DbException, Boolean] = for {
    database <- ZIO.service[Database.Service]
    result <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select EXISTS(select * from reservation where name = ${info.name} and phone = ${info.phone})""".stripMargin
            .query[Boolean]
            .unique
        }
      } yield res)
  } yield result

  def updateReservation(
      target: Reservation
  ): ZIO[doobie.Database.Service, DbException, Unit] = for {
    database <- ZIO.service[Database.Service]
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|update reservation set (name, phone, restaurant_id, reservation_date, reservation_time, guests) VALUES
                    |(${target.userInfo.name},
                    | ${target.userInfo.phone},
                    | ${target.restaurant_id},
                    | ${target.reservation_date},
                    | ${target.reservation_time},
                    | ${target.guests}
                    | ${None})
            """.stripMargin.update.run
        }}
        yield res)
      } yield()


  def cancelReservation(target: UserInfo): ZIO[doobie.Database.Service, DbException, Unit] = for {
    database <- ZIO.service[Database.Service]
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|delete from reservation where name = ${target.name} and phone = ${target.phone}""".stripMargin.update.run
        }
       
      } yield res)
  } yield ()


}