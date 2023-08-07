# 자전거 대여 시스템

> 이 프로젝트는 자전거 대여 시스템을 간략하게 구현한 프로젝트입니다. 기본적인 자전거 대여 및 반납 기능과 로그인 기능을 제공합니다.
> Docker와 postgresql을 이용해 데이터베이스를 구축했고, SQL 쿼리는 스칼라의 doobie 라이브러리를 이용했습니다.

도커를 이용해 데이터베이스를 구축하려면 아래의 명령어들을 입력한 뒤, 데이터베이스를 추가하면 됩니다.

```bash
    docker run -p 5400:5400 --name bicycle -e POSTGRES_PASSWORD=<password> -d postgres
```

이 프로젝트에서 사용한 데이터베이스의 구조는 다음과 같습니다.

```bash
// users
CREATE TABLE users (
    id text NOT NULL,
    password text,
    balance integer,
    PRIMARY KEY (id)
);

// station
CREATE TABLE station (
    stationId integer NOT NULL,
    availableBikes integer,
    PRIMARY KEY (stationId)
);

// rental_record
CREATE TABLE rental_record (
    userId text NOT NULL,
    stationId integer,
    endStation integer,
    rentalTime integer,
    cost integer,
    FOREIGN KEY (userId) REFERENCES users(id),
    FOREIGN KEY (stationId) REFERENCES station(stationId),
    FOREIGN KEY (endStation) REFERENCES station(stationId)
);
```

## 구조

각 역할 별로 패키지를 나누어 구현했습니다. 다음은 파일들의 역할입니다.

- `BicycleRentalApp.scala`: 프로그램의 진입점입니다. 유저의 입력을 받아 각 비즈니스 로직을 수행합니다.
- `User.scala`
        - `UserServices`: 유저 정보를 추가하거나 삭제하는 것과 같은 유저 관련 DB 작업을 관리하는 클래스입니다.
- `Station.scala`
        - `StationServices`: 대여소 관련 DB 작업을 수행합니다. 대여소 정보를 추가하거나, 수정하거나, 삭제하는 역할을 합니다.
- `RentalRecord.scala`
        - `RentalRecordServices`: 대여 기록 관련 DB 작업을 수행합니다. 대여 기록을 추가하거나, 수정하거나, 삭제하는 역할을 수행합니다.
- `BicycleRentalService.scala`
        - 자전거 대여 서비스의 비즈니스 로직을 구현합니다.
        - 자전거 대여, 반납, 자전거 대여소에서 자전거가 사용 가능한지 체크하는 기능을 제공합니다.
        - 또한, 로그인 기능을 제공합니다.
