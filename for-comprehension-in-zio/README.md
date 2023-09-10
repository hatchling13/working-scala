# ZIO에서의 for-comprehension
## 의존성
```scala
"dev.zio" %% "zio" % zioVersion
```

## 개요
ZIO는 함수형 프로그래밍에 그 기반이 있는 라이브러리이며, 함수형 프로그래밍에서는 부수 효과를 될 수 있는 한 제거하고 싶어합니다. 부수 효과가 없는 함수(즉, 순수 함수)는 다른 함수의 결과에 영향을 미칠 수 없어, 특정 함수의 동작을 이해하려 할 때 명시된 입출력 외에 작업자가 추가로 신경써야 될 정보가 줄어들기 때문입니다. 그러나 컴퓨터를 이용하여 처리해야 하는 실세계의 작업들은 대개 부수 효과를 포함하기 마련입니다. 함수형 프로그래밍에서는 이러한 문제를 '모나드'라는 개념을 이용하여 해결하고 있으며, ZIO 또한 마찬가지입니다. ZIO의 핵심 자료형인 `ZIO` 타입은 모나드의 특성을 가지고 있기 때문에 '모나딕 타입'이라고 부를 수 있으며, 이 덕분에 `ZIO` 타입에 for문을 적용할 수 있습니다.

### 모나딕 타입
어떠한 타입이 모나딕 타입이 되기 위해서는 두 가지 종류의 연산이 정의되어야 합니다. `ZIO` 타입은 다음과 같이 두 가지 연산을 모두 정의하고 있습니다:
1. 임의의 타입 `A`를 `ZIO[R, E, A]` 타입으로 만들어주는 연산: 대표적으로 `ZIO.succeed()` 함수가 있으며, 이외에도 다양한 함수들을 소개하는 [공식 문서](https://zio.dev/overview/creating-effects)가 있습니다.
2. 임의의 타입 `A`, `B`에 대해 `ZIO[R1, E1, A]`를 `ZIO[R2, E2, B]`로 만들어주는 연산: `ZIO.flatMap()` 함수를 이용하여 두 `ZIO` 타입을 순차적으로 연결해줄 수 있으며, [공식 문서](https://zio.dev/overview/basic-operations/#chaining)에 그 용례가 나와있습니다.

자세한 구현체는 [여기](https://github.com/zio/zio/blob/series/2.x/core/shared/src/main/scala/zio/ZIO.scala)서 확인할 수 있습니다.

### ZIO 타입에 for문 적용하기
[Scala의 for-comprehension 문서](/for-comprehension-in-scala/README.md)에서 확인할 수 있듯, Scala 컴파일러는 `for`문을 사용한 코드를 만나면 이를 적절한 형태의 고차 함수로 변환합니다. `ZIO` 타입은 [`flatMap`](https://github.com/zio/zio/blob/b38e4d30c364aa4da0b19bff6acc496e39fc81a8/core/shared/src/main/scala/zio/ZIO.scala#L628)이 정의되어있고, 추가적으로 [`map`](https://github.com/zio/zio/blob/b38e4d30c364aa4da0b19bff6acc496e39fc81a8/core/shared/src/main/scala/zio/ZIO.scala#L959) 또한 정의되어있습니다. `for`문을 사용하여 `ZIO` 타입을 다루게 되면 Scala 컴파일러가 각 코드를 `ZIO` 타입의 `map`이나 `flatMap`으로 적절하게 변환해줍니다.

```scala
import zio._

val getTheAnswer = ZIO.succeed(42)

// 1. for 키워드를 사용한 ZIO Effect
val effectWithFor = for {
  answer <- getTheAnswer
  answerText = answer.toString()
  _ <- Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answerText!")
} yield ()

// 2. 고차 함수를 사용한 ZIO Effect
val effectWithHOF = getTheAnswer
  .map(answer => answer.toString())
  .flatMap(answerText =>
    Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answerText!")
  )
```
