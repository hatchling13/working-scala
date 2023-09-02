import zio._
import Util._
import Repository._

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

  def readReservation(restaurant_id: String): ZIO[Any, Exception, Reservation] =
    for {
      name <- readLine("예약자 이름을 입력해주세요 : ")
      phone <- readLine("예약자 전화번호 뒤 4자리를 입력해주세요 : ")
      reservation_date <- readLine("예약 날짜를 입력해주세요 (ex:0730) : ")
      reservation_time <- readLine("예약 시간을 입력해주세요 (ex:1430) : ")
      guests <- readLine("인원 수를 숫자로 입력해주세요 : ")

      reservation = Reservation(
        name,
        phone,
        Integer.parseInt(restaurant_id),
        reservation_date,
        reservation_time,
        Integer.parseInt(guests)
      )
    } yield reservation
}
