# DB-Connection

## 의존성

```sbt
  "dev.zio" %% "zio-http" % "3.0.0-RC2",
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
  "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
  "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
  "org.postgresql" % "postgresql" % "42.5.4",
```

- [doobie](https://tpolecat.github.io/doobie/) : Scala와 Cats를 위한 순수 함수형 JDBC
  - JDBC : 데이터베이스와 연결되어 데이터를 주고 받을 수 있게 해주는 인터페이스
- [Tranzatio](https://github.com/gaelrenoux/tranzactio) : Doobie와 같은 몇가지 스칼라 데이터베이스 접근 라이브러리를 위한 ZIO Wrapper

## 개요

db-connection은 실제 Database와 연동을 해보고 CRUD를 해보는 프로젝트입니다.
Main 파일을 실행하면 입력, 업데이트, 삭제 순으로 확인 가능하며 Read기능은 입력, 업데이트, 삭제 이후 가져와 로그로 확인할 수 있습니다.

## 환경설정

프로젝트를 실행하기에 앞서서 로컬 환경에 Database세팅이 되어 있어야합니다.
(postgresql 또는 sqlite)

## 파일설명

- DBConnection.scala : db 연결하는 기능에 대한 클래스가 있습니다. postgresql과 sqlite에 대한 예시가 있습니다.
  - 현재 세팅은 postgresql기준으로 작성되어 있습니다. 만약 db를 sqlite로 변경하여 사용하고 싶은 경우 DBConnection.scala파일 31번째 줄의 postgres를 지우고, sqlite를 입력해주세요.
  - **현재 세팅된 DB Table 이름은 Person입니다. (postgresql은 person으로 입력하면 잘 실행됩니다.).**
- Main.scala: 메인클래스 입니다. 간단한 CRUD를 실행시키고 DB 연결을 진행합니다.
- DBService.scala : CRUD 기능, 테이블 생성 및 테이블이 있는지 확인하는 메소드가 있는 클래스 입니다.

## 메소드 목록

- tzio : query wrapper
- transactionOrWiden : tranzactio에서 제공하는 3가지 종류의 transaction method 중 하나, 쿼리 에러 타입이 DBException 또는 바로 Exception 타입일 경우 다른 transaction method 보다 유용합니다. 다른 transaction method에 대한 정보는 [이 곳](https://github.com/gaelrenoux/tranzactio#running-a-query-detailed-version)을 참고해주세요.
