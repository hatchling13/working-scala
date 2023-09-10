# Scala의 for-comprehension

## 개요
Scala 코드에서 발견할 수 있는 `for`문은 언뜻 보면 다른 명령형 언어들의 `for`문과 비슷해보이지만, 그 속을 들여다보면 그리 만만한 녀석이 아니라는 것을 알 수 있습니다. 

Scala의 `for`문은 `Iterable`을 상속받은 여러 가지 `Collection`에 [고차 함수](https://ko.wikipedia.org/wiki/%EA%B3%A0%EC%B0%A8_%ED%95%A8%EC%88%98)를 적용하기 위해 쓰이는, Scala만의 특징적인 요소입니다. `for`문이 어떤 모습으로 사용되었느냐에 따라, Scala 컴파일러가 각각의 `for`문 코드를 적절한 형태의 고차 함수로 변환합니다. 다시 말하자면, Scala의 `for`문은 사실 고차 함수를 간단하게 사용하기 위해 언어 차원에서 제공하는 ['문법적 설탕'(Syntactic Sugar)](https://hjaem.info/articles/kr_2008_1)이라고 할 수 있습니다. 무슨 말인지 차근차근 따라가봅시다.

### for <-> foreach
```scala
var sum = 0

// 1. for 키워드를 사용한 반복
for (i <- (1 until 10)) sum += i

// 2. 고차함수 foreach를 사용한 반복
(1 until 10).foreach(i => sum += i)
```

### for <-> map
```scala
// 1. for 키워드를 사용한 변환
val list = for (i <- 1 to 5) yield i * 2

// 2. 고차함수 map을 사용한 변환
val list = (1 to 5).map(i => i * 2)
```

### for <-> withFilter
```scala
// 1. for 키워드를 사용한 필터링
for {
  i <- 1 to 10
  if i % 2 == 0
} yield i

// 2. 고차함수 foreach, withFilter(또는 filter)를 사용한 필터링
(1 to 10).withFilter(i => i % 2 == 0).foreach(x => println(x))
```

### for <-> flatMap
```scala
def quotient(a: Int, b: Int) = if (b == 0) None else Some(a / b)

val data = List((3, 2), (10, 3), (20, 0), (4, 1))

// 1. for 키워드를 사용한 변환
val result = for {
  tuple <- list
  res <- quotient(tuple._1, tuple._2)
} yield res

// 2. 고차함수 flatMap을 사용한 변환
val result = list.flatMap(tuple => quotient(tuple._1, tuple._2))
```

### 비교 및 분석
각각의 경우에 대해 두 코드는 동일한 작업을 수행하며, 더 나아가서 Scala에서 두 코드는 서로 동치입니다. Scala 컴파일러가 첫번째 형태의 코드를 두번째 형태로 변환하기 때문입니다. 따라서 작업자의 기호에 따라 어떤 스타일로 작성할 지 선택할 수 있습니다. 코드를 좀 더 순차적으로 보이게 작성해서 프로그램의 흐름을 알기 쉽게 작성하고 싶다면 `for`문을 적극적으로 이용하는 것이 좋고, 각 작업 간의 의존성이 더 명시적으로 보이는 것이 좋다면 고차 함수 형태로 작성하는 것이 좋습니다.