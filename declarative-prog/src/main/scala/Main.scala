import zio._
import Util._

object Main extends ZIOAppDefault {

  val prog = for {
    
    action <- Controller.getAction()

    _ <- action match {
      case "예약하기" => for { // 1. 식당 선택
    restaurant_list <- Controller.showAllRestrauntsToUser()
    target_restaurant <- Controller.selectRestaurantByNumber()

    // 2. 예약 정보 입력
    _ <- Controller.registerReservation(target_restaurant)

      } yield ()
      case "예약변경" => Controller.checkReservationByInfo()
      case _ => ZIO.fail("잘못된 액션입니다.")
     }
  } yield ()


  override def run = prog
    .provide(Postgres.DBLayer)
}
