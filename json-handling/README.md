# JSON Handling

## 개요

[ZIO JSON](https://github.com/zio/zio-json) 라이브러리를 사용하여 JSON을 다루는 예제이다.

`TODO`라는 JSON 데이터 형식을 정의하고, 인코딩과 디코딩을 수행하는 코드를 작성해보자.

> ZIO JSON을 처음 다루신다면 다음 목차 [주요 API](#주요-api)를 먼저 읽어보세요.

## 의존성

```sbt
"dev.zio" %% "zio" % zioVersion
"dev.zio" %% "zio-test" % zioVersion % Test
"dev.zio" %% "zio-test-sbt" % zioVersion % Test
"dev.zio" %% "zio-json" % zioJsonVersion
```

## 메서드 목록

- `decode(string: String): Task[Todo]`
  - JSON 문자열을 `Todo` 객체로 변환한다.
  - 만약 변환에 실패하면 `Exception` 예외가 발생한다.

## 주요 API

ZIO JSON의 `JsonDecoder`와 `JsonEncoder`을 사용하여 JSON과 Object 간의 변환을 수행할 수 있다.

> 여기서 사용하는 예제 코드는 [simple.sc](./src/main/scala/simple.sc)를 참고해주세요.

### JsonDecoder

만약 다음과 같은 JSON이 있다고 가정하자.

```json
{"curvature":0.5}
```

이 JSON을 다음 Scala case class로 반환해보자.

```scala
case class Banana(curvature: Double)
```

이를 위해서는 다음과 같이 `JsonDecoder`를 정의해야 한다.

```scala
object Banana {
  implicit val decoder: JsonDecoder[Banana] = DeriveJsonDecoder.gen[Banana]
}
```

이제 다음과 같이 `fromJson`을 사용하여 JSON을 Object로 변환할 수 있다.

```scala
scala> """{"curvature":0.5}""".fromJson[Banana]
val res: Either[String, Banana] = Right(Banana(0.5))
```

### JsonEncoder

이번에는 Object를 JSON으로 변환해보자.

```scala
val banana = Banana(0.5)
```

이를 위해서는 다음과 같이 `JsonEncoder`를 정의해야 한다.

```scala
object Banana {
  implicit val encoder: JsonEncoder[Banana] = DeriveJsonEncoder.gen[Banana]
}
```

이제 다음과 같이 `toJson` 또는 `toJsonPretty`를 사용하여 Object를 JSON으로 변환할 수 있다.

```scala
scala> Banana(0.5).toJson
val res: String = {"curvature":0.5}

scala> Banana(0.5).toJsonPretty
val res: String =
{
  "curvature" : 0.5
}
```

> `toJsonPretty`는 JSON을 위와 같이 보기 좋게 출력해줍니다.
