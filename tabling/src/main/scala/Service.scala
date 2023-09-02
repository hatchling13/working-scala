import zio._
import Util._
import Repository._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import _root_.doobie.implicits._
import _root_.doobie._

object Service {
  def calculateRateByGuestNumber(reservation: Reservation): Option[Int] =
    reservation.guests match {
      case guests if guests > 0 && guests < 10 => Some(guests * 10)
      case guests if guests >= 10              => Some(100)
      case _                                   => None
    }

  // 인원 수에 맞게 할인율을 계산한 쿠폰을 발급하는 함수입니다.
  def issueCoupon(reservation: Reservation, discountRate: Int) =
    for {
      _ <- saveCoupon(Coupon(reservation.name, discountRate))
    } yield ()

  def checkIfCustomerGetCoupon(guests: Int): Boolean = guests > 5


  def makeReservation(info: ReservationInfo) = for {

    database <- ZIO.service[Database.Service]
    reservationList <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select * from reservation where name = ${info.name} and phone = ${info.phone}""".stripMargin
            .query[Reservation]
            .to[List]
        }
      } yield res)

    // 입력한 정보에 해당하는 예약이 있을 경우만 수정/삭제 가능
    _ <- reservationList.size match {
      case 0 => zio.Console.printLine("입력하신 정보에 해당하는 예약이 없습니다.")
      case _ =>
        for {
          // TODO: 예약 리스트 출력 포맷 변경 (1,2,3, ... 예약 인덱스)
          _ <- zio.Console.printLine(reservationList)
          choice <- readLine("수정/삭제하실 예약 번호를 선택하세요 : ")
          target = reservationList(Integer.parseInt(choice))
          func <- readLine("수정(1) / 삭제(2) : ")
          _ <- func match {
            case "1" => updateReservation(database, target)
            case "2" => deleteReservation(database, target)
          }
        } yield ()
    }
  } yield ()
}
