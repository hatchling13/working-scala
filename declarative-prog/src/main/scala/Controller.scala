import zio._
import Util._
import Repository._
import Service._
import Util._

object Controller {
  def getAction() =
    for {
      input <- readLine("예약을 하시려면 1, 변경/취소하시려면 2를 입력해주세요.")
      _ <- input match {
        case "1" => Controller.doReservation()
        case "2" => Controller.selectNextAcionFromUser()
        case _   => ZIO.fail("잘못된 입력입니다")
      }
    } yield ()

  def doReservation() =
    for {
      // 전체 식당 조회
      _ <- ZIO.unit
      restraunt_list <- Repository.getAllRestaurantList()
      _ <- zio.Console.printLine(s"${restraunt_list}")
      _ <- zio.Console.printLine("예약 가능한 식당의 목록입니다.")

      // 번호로 예약할 식당 선택
      number <- readLine("예약을 원하시는 식당의 번호를 입력해주세요 : ")
      id = Util.toInt(number)
      target_restaurant <- Repository.findRestaurantById(id)
      target_reservation <- Controller.getReservationInputValueFromUser(target_restaurant)
      _ <- Repository.makeReservationToRestaurant(target_restaurant, target_reservation)

      // 쿠폰 발급
      _ <- Service.issueCoupon(target_reservation)
      _ <- zio.Console.printLine("인원수에 맞는 쿠폰이 발급됩니다.")

      result <- Repository.getReservationByInfo(target_reservation.userInfo)
      _ <- zio.Console.printLine(s"${result.userInfo.name}님! 예약이 완료되었습니다.")

    } yield ()

  def selectNextAcionFromUser() =
    for {
      input <- readLine("예약을 변경하시려면 1, 취소하시려면 2를 입력해주세요.")
      info <- Controller.getUserInfoByUser()
      _ <- input match {
        case "1" => 

          ZIO.ifZIO(Repository.checkIfReservationExist(info))(
            onTrue = Controller.changeReservation(info),
            onFalse = zio.Console.printLine("입력하신 정보에 해당하는 예약이 없습니다.")
          )

        case "2" =>
          Repository.cancelReservation(info)
          ZIO.ifZIO(Repository.checkIfReservationExist(info))(
            onTrue = zio.Console.printLine("취소되었습니다."),
            onFalse = zio.Console.printLine("취소에 실패했습니다.")
          )
      }
    } yield ()

  def changeReservation(info: UserInfo) =
    for {
      reservationList <- Repository.getReservationByInfo(info)

      _ <- zio.Console.printLine(reservationList)
      reservationNumber <- readLine("변경을 원하는 예약의 예약번호를 입력해주세요.")
      index = Integer.parseInt(reservationNumber)

      targetReservation <- Repository.getReservationByNumber(index)
      targetRestaurant <- Repository.findRestaurantById(index)

      newReservation <- Controller.getChangedReservationInputValueFromUser(targetReservation, targetRestaurant)      
      _ <- Repository.updateReservation(newReservation)
      _ <- zio.Console.printLine("예약이 변경되었습니다.")
    } yield ()

  def getChangedReservationInputValueFromUser(reservation: Reservation, restaurant: Restaurant) =
    for{
      
      reservation_date <- readLine("변경할 예약 날짜를 입력해주세요 (ex:0730) : ")
      reservation_time <- readLine("변경할 예약 시간을 입력해주세요 (ex:1430) : ")
      guests <- readLine("변경할 인원 수를 숫자로 입력해주세요 : ")

      
      changedReservation = Reservation(
        reservation.userInfo,
        restaurant.id,
        reservation_date,
        reservation_time,
        Util.toInt(guests),
        None
      )

    } yield(changedReservation)


  def getReservationInputValueFromUser(targetRestaurant: Restaurant) =
    for{
      name <- readLine("예약자 이름을 입력해주세요 : ")
      phone <- readLine("예약자 전화번호 뒤 4자리를 입력해주세요 : ")
      reservation_date <- readLine("예약 날짜를 입력해주세요 (ex:0730) : ")
      reservation_time <- readLine("예약 시간을 입력해주세요 (ex:1430) : ")
      guests <- readLine("인원 수를 숫자로 입력해주세요 : ")

      userInfo = UserInfo(name, phone)

      reservation = Reservation(
        userInfo,
        targetRestaurant.id,
        reservation_date,
        reservation_time,
        Integer.parseInt(guests),
        None
      )
    } yield(reservation)

    def getUserInfoByUser() = for {
      name <- readLine("이름을 선택해주세요 : ")
      phone <- readLine("전화번호 뒤 4자리를 입력해주세요 : ")

      info = UserInfo(name, phone)
    } yield (info)
}
