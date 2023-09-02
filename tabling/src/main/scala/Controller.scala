import zio._
import Util._

object Controller {
  
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

    def readReservationInfo() = for {
        name <- readLine("이름을 선택해주세요 : ")
        phone <- readLine("전화번호 뒤 4자리를 입력해주세요 : ")
    } yield ReservationInfo(name, phone)

    def readRestaurant(list: List[Restaurant]) = for {
        _ <- zio.Console.printLine(list)
        input <- readLine("식당을 번호로 입력해주세요 : ")
    } yield input
}
