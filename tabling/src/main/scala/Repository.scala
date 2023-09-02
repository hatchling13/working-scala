import zio._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import io.github.gaelrenoux.tranzactio.{ConnectionSource, DbException, doobie}
import _root_.doobie.implicits._
import _root_.doobie._
import Util._

object Repository {

  def saveReservation(reservation: Reservation): ZIO[doobie.Database.Service, DbException, Unit] = for {
    db <- ZIO.service[Database.Service]
    insertResult <- db
      .transactionOrWiden(for {
      res <- tzio {
        sql"""|insert into reservation values
                    |(${reservation.name},
                    |${reservation.phone},
                    |${reservation.restaurant_id},
                    |${reservation.reservation_date},
                    |${reservation.reservation_time},
                    |${reservation.guests})""".stripMargin
        .update()
        .run
      }
    } yield ())
    // _ = zio.Console.printLine(s"${reservation.name}님! 예약이 완료되었습니다.")
  } yield ()

  def saveCoupon(coupon: Coupon): ZIO[doobie.Database.Service, DbException, Unit] = for {
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
    // _ = zio.Console.printLine(s"${coupon.discount_rate}% 쿠폰이 발급되었습니다.")
  } yield ()

  def findAllRestaurantList(): ZIO[doobie.Database.Service, Exception, List[Restaurant]] = for {
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

  def isRestaurantExist(restaurant_id: String): ZIO[doobie.Database.Service, DbException, Boolean] = for {
    database <- ZIO.service[Database.Service]
    result <- database
      .transactionOrWiden(for {
        res <- tzio {
        sql"""|select EXISTS(select * from restaurant where id = ${Integer.parseInt(restaurant_id)})""".stripMargin
      .query[Boolean]
      .unique
      }
    } yield res)
  } yield result

    def updateReservation(database: doobie.Database.Service, target: Reservation): ZIO[Any, DbException, Unit] = for {
    // TODO : 예약 수정 정보를 입력받아서 update
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|update reservation set reservation_time='0000' where name = ${target.name} and phone = ${target.phone}""".stripMargin
            .update
            .run
        }
      } yield res)
  } yield ()

  def deleteReservation(database: doobie.Database.Service, target: Reservation): ZIO[Any, DbException, Unit] = for {
    _ <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|delete from reservation where name = ${target.name} and phone = ${target.phone}""".stripMargin
            .update
            .run
        }
      } yield res)
    _ = zio.Console.printLine("삭제되었습니다.")
  } yield ()

}
