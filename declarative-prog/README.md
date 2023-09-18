# 선언적 프로그래밍
## 개요
선언형 프로그래밍은 **무엇을 할 것인지**에 집중하고, 어떻게 할 것인지는 명령적인 부분으로 분리합니다.
프로그램이 어떻게 동작할지 계획하는 부분(선언)과 프로그램이 어떻게 실행될 지 나타내는 부분(실행)이 명시적으로 분리되어 있다면 요구사항이 변경 되었을 때 유리할 것입니다.

다시 말해, 프로그램이 실행되는 데 필요한 외부 제약조건(실행 시간 등)을 실제 동작과 분리할 수 있습니다.
추상화의 수준이 높아질 수록 동작을 요청하는 주체는 구현의 세부사항에 대해 주의를 기울이지 않아도 됩니다.

ZIO의 **선언적 인코딩**, **선언적 에러처리**에 대한 내용이 궁금하시다면 다음 링크를 참고해주세요.


  - [DeepZIO3 정리자료](https://www.notion.so/DeepZIO3-90fd596582994cfe91ed1793fe57712a?pvs=4)
  - [DeepZIO4 정리자료](https://www.notion.so/DeepZIO4-5e1bfe48c387458a873148c33ff0560e?pvs=4)


## [요구사항] 쿠폰 발급하기

- DB에 Coupon table을 만들고 string type의 owner와 int type의 discount column을 만드세요. (owner는 Reservation table의 name의 외래키입니다)
- Reservation table에  데이터를 넣고 해당 예약의 인원 수 만큼 해당 예약자(Reservation.name)에게 discount 쿠폰을 발급해주세요.


## 의존성
```scala
lazy val `declarative-prog` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
      "org.postgresql" % "postgresql" % "42.5.4"
    )
  )
```


---

### 메소드 목록
#### `Controller`
- getAction(): ZIO[Any,Serializable,String]
  - 예약하기/예약변경 둘 중 하나의 액션을 숫자 입력으로 받습니다.
- showAllRestrauntsToUser(): ZIO[Database.Service,Exception,Unit]
  - 예약 가능한 모든 레스토랑의 리스트를 출력합니다.
- registerReservation(restaurant: Restaurant): ZIO[Database.Service,Serializable,Unit]
  - 예약을 등록합니다.
- checkReservationByInfo(): ZIO[Database.Service,Serializable,Unit]
  - 예약 정보를 통해 기존 예약이 존재하는 지 확인합니다.
- parseNextAction(input: String): ZIO[Any,String,String]
  - 유저의 입력과 실행될 함수를 매치시켜주는 함수입니다.
- selectNextAcionFromUser(info: ReservationInfo): ZIO[Database.Service,Serializable,Unit]
  - 유저의 다음 액션에 따라 다른 함수를 실행합니다.
- changeReservationByNumber(info: ReservationInfo): ZIO[Database.Service,Exception,Unit]
  - 예약을 번호를 통해 변경합니다.
- selectRestaurantByNumber(): ZIO[Database.Service,Exception,Restaurant]
  - 예약을 하려는 레스토랑 번호를 선택합니다.

#### `Service`
- calculateRateByGuestNumber(reservation: Reservation): Option[Int]
  - 예약인원에 따라 쿠폰의 할인율을 계산합니다.
- issueCoupon(reservation: Reservation): ZIO[io.github.gaelrenoux.tranzactio.doobie.Database.Service,Serializable,Unit]
  - 쿠폰을 생성합니다.

#### `Repository`
- findRestaurantById
  - 레스토랑의 ID를 통해 레스토랑을 조회합니다.
- getReservationByInfo
  - 예약 시 입력했던 정보(이름, 전화번호 뒷 4자리)를 통해 예약을 조회합니다.
- saveReservation
  - 예약을 저장합니다.
- saveCoupon
  - 발행된 쿠폰을 저장합니다.
- getAllRestaurantList
  - 모든 레스토랑의 리스트를 조회합니다.
- checkIfRestaurantExist
  - 레스토랑의 존재여부를 확인합니다.
- checkIfReservationExist
  - 예약의 존재여부를 확인합니다.
- updateReservation
  - 예약을 수정합니다.
- cancelReservation
  - 예약을 취소합니다.



### 예제 설명
선언적 에러처리에 대하여 다음과 같은 예시가 포함되어 있습니다.
```scala
object Service {
  // calculateRateByGuestNumber는 예약자 수에 따라 할인율을 계산하는 함수입니다.
  // 쿠폰의 할인율을 계산하여 나온 결과값에 대한 경우의 수는 예측 가능합니다.
  // 이처럼 예측 가능한 결과를 타입으로 만들어 패턴 매칭하고 Option[Int]를 리턴하게 한다면
  // 에러처리를 보다 예상 가능하게 할 수 있습니다.
  def calculateRateByGuestNumber(reservation: Reservation): Option[Int] =
    reservation.guests match {
      case guests if guests > 0 && guests < 10 => Some(guests * 10)
      case guests if guests >= 10              => Some(100)
      case _                                   => None
    }

  // 인원 수에 맞게 할인율을 계산한 쿠폰을 발급하는 함수입니다.
  def issueCoupon(reservation: Reservation) =
    for {
      discountRate <- calculateRateByGuestNumber(reservation)
      target_coupon = Coupon(reservation.name, discountRate)
      _ = Repository.saveCoupon(target_coupon)
    } yield ()
}

```

### 참고문서

- https://zio.dev/reference/error-management/imperative-vs-declarative/
- https://degoes.net/articles/functional-design