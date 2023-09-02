import zio._
import Util._

object Main extends ZIOAppDefault {

  val prog = for {
    _ <- ZIO.unit
    // 1. 식당 선택
    restaurant_id <- Repository.readRestaurant()
    isExist <- Repository.isRestaurantExist(restaurant_id)
    // 식당이 존재하지 않으면 종료
    _ <- ZIO.when(!isExist)({
      zio.Console.printLine("잘못 선택하셨습니다. 프로그램을 종료합니다.")
      sys.exit(-1)
    })

    // 2. 예약 정보 입력
    reservation <- Service.readReservation(restaurant_id)
    _ = zio.Console.printLine("인원수에 맞는 쿠폰이 발급됩니다.")
    // 3. 쿠폰 발급
    rate <- ZIO.fromOption(Service.calculateRateByGuestNumber(reservation))
    _ <- Service.issueCoupon(reservation, rate)
    // 4. 예약 정보 저장
    _ <- ZIO
      .attempt(Repository.saveReservation(reservation))
      .catchAll(e => zio.Console.printLine(s"${e} 예상치 못한 오류가 발생했어요."))
  } yield ()

  override def run = prog
    .provide(Postgres.DBLayer)
}
