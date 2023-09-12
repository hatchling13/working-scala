import zio._
import Util._
import Repository._
import Service._

object Controller {
  def getAction() =
    for {
      input <- readLine("예약을 하시려면 1, 변경하시려면 2를 입력해주세요.")
      action <- input match {
        case "1" => ZIO.succeed(doReservation)
        case "2" => ZIO.succeed(changeReservation)
        case _   => ZIO.fail("잘못된 입력입니다")
      }
    } yield action

  def doReservation() =
    for {
      // 1. 전체 식당 조회
      restaurant_list <- Controller.showAllRestrauntsToUser()
      // 2. 식당 선택
      target_restaurant <- Controller.selectRestaurantByNumber()
      _ <- Controller.registerReservation(target_restaurant)

    } yield ()

  def changeReservation() =
    for {
      _ <- Controller.checkReservationByInfo()
    } yield ()

  def showAllRestrauntsToUser() =
    for {
      restraunt_list <- Repository.getAllRestaurantList()
      _ <- readLine(s"${restraunt_list}")
    } yield ()

  def registerReservation(restaurant: Restaurant) =
    for {
      name <- readLine("예약자 이름을 입력해주세요 : ")
      phone <- readLine("예약자 전화번호 뒤 4자리를 입력해주세요 : ")
      reservation_date <- readLine("예약 날짜를 입력해주세요 (ex:0730) : ")
      reservation_time <- readLine("예약 시간을 입력해주세요 (ex:1430) : ")
      guests <- readLine("인원 수를 숫자로 입력해주세요 : ")

      reservation = Reservation(
        name,
        phone,
        restaurant.id,
        reservation_date,
        reservation_time,
        Integer.parseInt(guests)
      )
      _ <- Repository.saveReservation(reservation)
      _ <- Service.issueCoupon(reservation)
      _ <- zio.Console.printLine("인원수에 맞는 쿠폰이 발급됩니다.")

    } yield ()

  def checkReservationByInfo() =
    for {
      name <- readLine("이름을 선택해주세요 : ")
      phone <- readLine("전화번호 뒤 4자리를 입력해주세요 : ")
      info = ReservationInfo(name, phone)

      result <- Repository.checkIfReservationExist(info)
      _ <- result match {
        case true  => Controller.selectNextAcionFromUser(info)
        case false => zio.Console.printLine("입력하신 정보에 해당하는 예약이 없습니다.")
      }
    } yield ()

  private def parseNextAction(input: String) = input match {
    case "1" => ZIO.succeed("change")
    case "2" => ZIO.succeed("cancel")
    case _   => ZIO.fail("invalid next action")
  }

  def selectNextAcionFromUser(info: ReservationInfo) =
    for {
      input <- readLine("예약을 변경하시려면 1, 취소하시려면 2를 입력해주세요.")
      nextAction <- parseNextAction(input)
      _ <- nextAction match {
        case "change" => changeReservationByNumber(info)
        case "cancel" => cancelReservation(info)
      }
    } yield ()

  def changeReservationByNumber(info: ReservationInfo) =
    for {
      reservationList <- Repository.getReservationByInfo(info)
      _ <- zio.Console.printLine(reservationList)
      reservation_number <- readLine("변경을 원하는 예약의 예약번호를 입력해주세요.")

    } yield ()

  def selectRestaurantByNumber() =
    for {
      number <- readLine("예약을 원하시는 식당의 번호를 입력해주세요 : ")
      id = Integer.parseInt(number)
      restaurant <- Repository.findRestaurantById(id)
    } yield restaurant
}
