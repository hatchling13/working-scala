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
  def issueCoupon(reservation: Reservation) =
    for {
      discountRate <- ZIO.fromOption(calculateRateByGuestNumber(reservation))
      target_coupon = Coupon(reservation.name, discountRate)
      _ <- Repository.saveCoupon(target_coupon)
    } yield ()

  // legacy
  // def checkIfCustomerGetCoupon(guests: Int): Boolean = guests > 5

}
