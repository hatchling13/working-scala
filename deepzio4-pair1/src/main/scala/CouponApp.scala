import zio._

object CouponApp extends ZIOAppDefault {
  def getCouponUsers() = for {
    users <- PostgreSQLService.selectFromTable[CouponUser](
      "coupon_user_table",
      List("name", "level")
    )
  } yield users

  def createCoupons(user: Either[String, List[CouponUser]]) = for {
    result <- user match {
      case Left(value) => for { _ <- ZIO.unit } yield Left(value)
      case Right(users) =>
        for {
          coupons <- ZIO.foreach(users) { user => makeCoupon(user, 7) }
          suitableCoupons = coupons.flatten
          _ <- PostgreSQLService.insertCouponTable(suitableCoupons)
        } yield Right(suitableCoupons)
    }
  } yield result

  def makeCoupon(user: CouponUser, vipLevel: Int) = if (
    user.level >= vipLevel
  ) {
    if (user.level >= 10) {
      ZIO.some(Coupon(user.name, 100))
    } else {
      ZIO.some(Coupon(user.name, user.level * 10))
    }
  } else { ZIO.none }

  private val program = for {
    user <- getCouponUsers
    coupons <- createCoupons(user)

    _ <- coupons match {
      case Left(errorMessage) => Console.printLineError(errorMessage).ignore
      case Right(coupons) =>
        ZIO.foreachDiscard(coupons) { coupon =>
          Console
            .printLine(s"Coupon for ${coupon.owner}: ${coupon.discount}%")
            .ignore
        }
    }
  } yield ()

  override def run = program.provide(
    PostgreSQLService.DBLayer
  )
}
