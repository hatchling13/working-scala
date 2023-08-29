import io.github.gaelrenoux.tranzactio.{ConnectionSource, DbException, doobie}
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import _root_.doobie._
import _root_.doobie.implicits._

import java.io.IOException

object Tabling extends ZIOAppDefault {

  def readLine(message: String): IO[IOException, String] = zio.Console.readLine(message)

  def readRestaurant(): ZIO[doobie.Database.Service, Exception, String] = for {
    database <- ZIO.service[Database.Service]
    // 예약 가능한 전체 식당 조회해서 출력
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select id, name
                |from restaurant""".stripMargin
            .query[Restaurant]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)
    input <- readLine("식당을 선택해주세요 : ")
  } yield input

  def readReservation(restaurant_id: String): ZIO[Any, IOException, Reservation] = for {
    name <- readLine("예약자 이름을 입력해주세요 : ")
    phone <- readLine("예약자 전화번호 뒤 4자리를 입력해주세요 : ")
    reservation_date <- readLine("예약 날짜를 입력해주세요 (ex:0730) : ")
    reservation_time <- readLine("예약 시간을 입력해주세요 (ex:1430) : ")
    guests <- readLine("인원 수를 숫자로 입력해주세요 : ")

    reservation = Reservation(name, phone, Integer.parseInt(restaurant_id), reservation_date, reservation_time, Integer.parseInt(guests))
  } yield reservation
  
  def isCouponReservation(guests: Int): Boolean = guests > 5

  def saveCoupon(coupon: Coupon): ZIO[doobie.Database.Service, DbException, Int] = for {
    db <- ZIO.service[Database.Service]
    insertResult <- db
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|insert into coupon values
                |(${coupon.owner},
                |${coupon.discount_rate})""".stripMargin
            .update()
            .run
        }
      } yield res)
  } yield insertResult

  def couponByGuests(reservation: Reservation): Option[Coupon] =
    reservation.guests match {
        case guests if guests > 0 && guests < 10 => Some(Coupon(reservation.name, guests * 10))
        case guests if guests >= 10 => Some(Coupon(reservation.name, 100))
        case _ => None
    }

  def saveCouponIfExists(coupon: Option[Coupon]) = coupon match {
    case Some(coupon) => saveCoupon(coupon)
    case _ => ZIO.unit
  }

  def saveReservation(reservation: Reservation): ZIO[doobie.Database.Service, DbException, Int] = for {
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
    } yield res)
  } yield insertResult

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

  val prog = for {
    _ <- ZIO.unit
    // 1. 식당 선택
    restaurant_id <- readRestaurant()
    isExist <- isRestaurantExist(restaurant_id)
    // 식당이 존재하지 않으면 종료
    _ <- ZIO.when(!isExist)({
      zio.Console.printLine("잘못 선택하셨습니다. 프로그램을 종료합니다.")
      sys.exit(-1)
    })

    // 2. 예약 정보 입력
    reservation <- readReservation(restaurant_id)

    // 3. 쿠폰 발급
    coupon = couponByGuests(reservation)
    _ <- saveCouponIfExists(coupon)
    
    // 4. 예약 정보 저장
    insertResult <- saveReservation(reservation)
    _ <- insertResult match {
      case 1 => zio.Console.printLine(s"예약이 완료되었습니다. $reservation")
      case _ => zio.Console.printLine(s"예약하다 뭔가 문제가 있어요.")
    }
  } yield ()

  override def run = prog
    .provide(
      conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
    ).exitCode

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        s"jdbc:postgresql://DB_PATH/DB_NAME?user=DB_USER&password=DB_PASSWORD"
      )
    )
  )
}
