case class Coupon(owner: String, discount_rate: Int) {}

case class Restaurant(id: Int, name: String)

case class Reservation(
    name: String,
    phone: String,
    restaurant_id: Int,
    reservation_date: String,
    reservation_time: String,
    guests: Int
)

case class ReservationInfo(
    name: String,
    phone: String
)
