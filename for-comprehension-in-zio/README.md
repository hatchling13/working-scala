# ZIO에서의 for-comprehension
## 의존성
```scala
"dev.zio" %% "zio" % zioVersion
```

## 개요
Scala에서의 `for`문은 언뜻 보면 다른 명령형 언어들과 비슷해보이지만, 그 속을 들여다보면 그리 만만한 녀석이 아니라는 것을 알 수 있습니다. Scala의 `for`문은 어떻게 쓰이느냐에 따라 각각 다른 형태의 [고차 함수](https://ko.wikipedia.org/wiki/%EA%B3%A0%EC%B0%A8_%ED%95%A8%EC%88%98)로 변환됩니다. 다시 말하자면, Scala의 `for`문은 사실 고차 함수를 간단하게 사용하기 위해 언어 차원에서 제공하는 ['문법적 설탕'(Syntactic Sugar)](https://hjaem.info/articles/kr_2008_1)이라고 할 수 있습니다. 무슨 말인지 차근차근 따라가봅시다.

### for <-> foreach
```scala
// 1. for 키워드를 사용한 반복
for (i <- (1 until 10)) println(i)

// 2. 고차함수 foreach를 사용한 반복
(1 until 10).foreach(i => println(i))
```

### for <-> withFilter
```scala
// 1. for 키워드를 사용한 필터링
for {
  i <- 1 to 10
  if i % 2 == 0
} {
  println(i)
}

// 2. 고차함수 foreach, withFilter(또는 filter)를 사용한 필터링
(1 to 10).withFilter(i => i % 2 == 0).foreach(x => println(x))
```

### for <-> map
```scala
// 1. for 키워드를 사용한 변환
val list = for (i <- 1 to 5) yield i * 2

// 2. 고차함수 map을 사용한 변환
val list = (1 to 5).map(i => i * 2)
```

### for <-> flatMap
```scala
def quotient(a: Int, b: Int) = if (b == 0) None else Some(a / b)

val list = List((3, 2), (10, 3), (20, 0), (4, 1))

// 1. for 키워드를 사용한 변환
val result = for {
  tuple <- list
  res <- quotient(tuple._1, tuple._2)
} yield res

// 2. 고차함수 flatMap을 사용한 변환
val result = list.flatMap(tuple => quotient(tuple._1, tuple._2))
```

### 비교 및 분석
각각의 경우에 대해 두 코드는 동일한 작업을 수행하며, 더 나아가서 Scala에서 두 코드는 서로 동치입니다. 컴파일러가 첫번째 형태의 코드를 두번째 형태로 변환하기 때문입니다. 따라서 작업자의 기호에 따라 어떤 스타일로 작성할 지 선택할 수 있습니다.

### ZIO 타입에 대해 for 사용하기
`ZIO` 타입은 `for`문을 사용하기 위해 필요한 고차 함수가 모두 구현되어 있습니다. `map`과 `flatMap`은 물론, `Iterable`을 상속받은 `Collection`에 대해 사용할 수 있는 `foreach`와 `filter`가 구현되어 있어 일반 Scala에서처럼 `for`문을 사용할 수 있습니다. 자세한 구현체는 [여기](https://github.com/zio/zio/blob/series/2.x/core/shared/src/main/scala/zio/ZIO.scala)서 확인할 수 있습니다. 

```scala
import zio._
import java.io.IOException

val getTheAnswer = ZIO.succeed(42)

// 1. for 키워드를 사용한 ZIO Effect
val effect1: ZIO[Any, IOException, Unit] = for {
  answer <- getTheAnswer
  _ <- Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answer!")
} yield ()

// 2. flatMap을 사용한 ZIO Effect
val effect2: ZIO[Any, IOException, Unit] = getTheAnswer.flatMap(
  answer => Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answer!")
)
```
