case class Coupon(owner: String, discount_rate: Int) {}

case class Restaurant(id: Int, name: String)


case class Reservation(
    userInfo: UserInfo,
    restaurant_id: Int,
    reservation_date: String,
    reservation_time: String,
    guests: Int,
    reservation_id: Option[Int] = None
)

case class UserInfo(
    name: String,
    phone: String
)
