# Error Handling

## 의존성

```sbt
  "dev.zio" %% "zio" % 2.0.13,
  "dev.zio" %% "zio-test" % 2.0.13 % Test,
  "dev.zio" %% "zio-test-sbt" % 2.0.13 % Test
  "dev.zio" %% "zio-json" % "0.3.0-RC10"
  "com.lihaoyi" %% "os-lib" % "0.9.1",
  "com.lihaoyi" %% "ujson" % "3.0.0",
```

예시를 위해 json파일을 읽을 수 있는 라이브러리를 설치했습니다.
(예제를 실행시키기 위해 실제로 파일은 필요하지 않습니다.)

## 개요

ZIO의 Error Handling 문서 한글화 및 예제 코드 프로젝트입니다.

## 메소드 목록

- either : error를 스칼라 Either type(failure 와 success)으로 변환시켜주는 메소드. 많은 ZIO operator들이 success channel에서 실행되기 때문에 유용하다.
- catchAll : 모든 타입의 recoverable error와 효과적으로 시도된 복구를 모두 handling하기 위해 사용하는 메소드.
- catchSome : 몇가지 타입의 recoverable error와 효과적으로 시도된 복구만 handling하기 위해 사용하는 메소드. catchAll과 다르게 에러 타입을 확장시킬 수 있지만, 에러 타입을 줄이거나 제거할 수 없다.
- orElse : 하나의 effect에 대해서 해당 effect가 실패했을 때 다른 effect를 실행하는 메소드
- fold : 기본 스칼라 메소드. 실패 및 성공에 대해서 일반적인 타입으로 변환시켜준다.
- foldZIO : ZIO 메소드. 실패 및 성공에 대해서 분리하여 특정 effect로 처리하도록 하는 메소드.
- retry : schedule을 사용하여 effect가 실패했을 때 원래의 effect를 [Schedule](https://zio.dev/reference/schedule/)에 따라서 다시 실행시켜주는 메소드.
- [retryOrElse](https://zio.dev/reference/error-management/recovering/retrying/#zioretryorelse) : retry 메소드 기능에서 fallback기능(orElse)이 추가된 메소드.
