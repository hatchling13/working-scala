import zio._
import Util._
import Repository._

object Main extends ZIOAppDefault {

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
    _ = zio.Console.printLine("인원수에 맞는 쿠폰이 발급됩니다.")
    // 3. 쿠폰 발급
    rate <- ZIO.fromOption(calculateRateByGuestNumber(reservation))
    _ <- issueCoupon(reservation, rate)
    // 4. 예약 정보 저장
    _ <- ZIO
      .attempt(saveReservation(reservation))
      .catchAll(e => zio.Console.printLine(s"${e} 예상치 못한 오류가 발생했어요."))
  } yield ()

  val makeReservation = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database.Service]

    name <- readLine("이름을 선택해주세요 : ")
    phone <- readLine("전화번호 뒤 4자리를 입력해주세요 : ")
    reservationList <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select * from reservation where name = $name and phone = $phone""".stripMargin
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
  override def run = prog
    .provide(Postgres.DBLayer)
}
