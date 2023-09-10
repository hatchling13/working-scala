# 의존성 주입(Dependency Injection)

## 개요
예제를 통해 ZIO 환경에서 의존성 주입에 대해 설명합니다.  
일반적인 의존성 주입 방식부터 ZIO를 활용한 방식까지 다루는 예제입니다.  
ZIO 의존성 주입에 대해 자세히 알고 싶다면 [ZIO 의존성 주입 공식 문서](https://zio.dev/reference/di/)를 참고하세요.

<br/>

두 가지 방식으로 의존성 주입을 다루어 보겠습니다.
1. [의존성 주입(DIExam.scala)](https://github.com/SHSongs/working-scala/blob/main/dependency-injection/src/main/scala/DiTest1.scala)
- 일반적으로 사용되는 의존성 주입을 표현한 코드입니다.
- 다른 언어에서도 동일하게 적용할 수 있습니다.
2. [ZIO 라이브러리를 활용한 의존성 주입(DIExam2.scala)](https://github.com/SHSongs/working-scala/blob/main/dependency-injection/src/main/scala/DiTest2.scala)
- ZIO에서 제공하는 메서드를 통해 의존성 주입 코드를 작성했습니다.
- 일반적인 의존성 주입 방식이라기 보다는 ZIO 라이브러리를 사용할 때 권장되는 방식입니다.



<br/><br/>

## 의존성
```scala
"dev.zio" %% "zio" % zioVersion
```



<br/><br/>

## 의존성 주입을 사용하는 이유
의존성 주입은 필요한 외부 객체를 서비스 내부에서 직접 생성하지 않고, 외부에서 제공받는 방식입니다.  
외부로부터 의존성을 제공받아 서비스 내에서 의존 객체를 직접 생성할 필요가 없도록 합니다.
```scala
def useCase() = for {
	_ <- ZIO.unit
	repo = new 판교식당저장소
	list <- repo.모든식당이름가져오기()
	_ <- zio.Console.printLine(list) 
} yield ()
```
위 코드를 살펴보면 판교의 모든 식당 이름을 불러오는 코드라는 것을 추측할 수 있습니다.  
만약 고객의 요청에 의해 공덕 식당 이름을 불러와야 하는 상황이 오게 되었다고 가정해 봅시다.  
그렇다면 아래와 같이 코드를 구현할 수 있을 것입니다.
```scala
def useCase() = for {
	_ <- ZIO.unit
	repo = new 공덕식당저장소
	list <- repo.모든식당이름가져오기()
	_ <- zio.Console.printLine(list) 
} yield ()
```
<br/>

그런데 이번엔 강남의 모든 식당 이름을 불러와달라는 요청이 들어왔습니다.  
개발자는 위와 비슷한 코드를 또 구현해야 할 것이고, 새로운 식당 이름을 요청 받을 때마다 동일한 작업을 반복해아할 것입니다.  
만약 repo 객체를 useCase 메서드로부터 분리함으로써 repo 객체에 따라 서로 다른 데이터가 표현되도록 할 수 있다면, 개발자는 많은 시간을 아낄 수 있을 것 같습니다.  

아래 코드에서 useCase를 서비스라고 하고 repo를 의존성이라고 생각하고 살펴보겠습니다.
```scala
trait 식당저장소 { def 모든식당이름가져오기(): ZIO[Any, Nothing, List[String]] }
class 공덕식당저장소 extends 식당저장소 { def 모든식당이름가져오기() = ZIO.succeed(List("식당1", "식당2")) }
class 판교식당저장소 extends 식당저장소 { def 모든식당이름가져오기() = ZIO.succeed(List("식당3", "식당4")) }
def useCase(repo: 식당저장소) = for {
	_ <- ZIO.unit
	list <- repo.모든식당이름가져오기()
	_ <- zio.Console.printLine(list)
} yield ()
```
위와 같이 repo라는 의존성을 useCase에 주입했습니다.  
앞의 코드와 두 가지 차이를 확인할 수 있습니다.  
1. repo 객체(의존성)를 직접 생성할 필요가 없어졌습니다.  
2. 식당저장소를 상속받는다면 필요에 따라 다른 식당 저장소까지 주입할 수 있습니다.

이제 개발자는 새로운 지역의 식당 이름을 불러오는 요청이 들어올 때마다 새로운 userCase를 작성하지 않아도 됩니다.  
아래와 같이 useCase를 호출하기만 하면 repo가 가진 식당 이름을 출력할 수 있게 되었습니다.  
```scala
_ <- useCase(new 판교식당저장소)
_ <- useCase(new 공덕식당저장소)
_ <- useCase(new 강남식당저장소)
```



<br/><br/>

## ZIO Effect에 의존성 주입하기
[DIExam1.scala](https://github.com/SHSongs/working-scala/blob/main/dependency-injection/src/main/scala/DiTest1.scala)에서는 일반적인 메서드에 의존성을 직접 주입했습니다.  
하지만 ZIO에서는 몇 가지 제공되는 메서드를 통해 ZIO Effect에 의존성을 제공하고, 주입받은 의존성을 사용할 수 있습니다.

### 관련 정보
- ZIO Effect란 ZIO[R, E, A]를 의미합니다. - [ZIO Effect R, E, A 알아보기](https://zio.dev/reference/core/zio/)
- 관련 메서드는 [메서드 목록](#메서드-목록)을 참고하시면 됩니다.



<br/><br/>

## 메서드 목록
`ZLayer.succeed()`  
값이나 서비스를 포함하는 레이어를 생성합니다.
- [ZLayer 알아보기](https://zio.dev/reference/contextual/zlayer/)

```scala
object 판교식당저장소 {
    val layer = ZLayer.succeed(new 판교식당저장소)
}
```
<br/>

`provideLayer()`  
ZLayer라는 계층을 통해 ZIO Effect에서 R에 해당하는 의존성을 제공해 줍니다.

```scala
// 판교식당저장소를 포함하는 의존성 계층을 생성하여, ZIO[R, E, A]에서 R 타입에 주입
_ <- useCaseUsingZLayer().provideLayer(판교식당저장소.layer) 
```
<br/>

`ZIO.service`  
ZIO 환경에서 특정 서비스에 접근할 수 있습니다.  
provideLayer 메서드를 통해 ZIO 환경에 의존성을 주입하였다면, ZIO.service를 통해 주입한 의존성을 가져와서 사용할 수 있습니다.    
따라서 ZIO.service의 대괄호[]에는 제공받은 의존성 타입이 와야 합니다.    
아래 코드를 살펴보겠습니다.

```scala
trait 식당저장소 {
  // ...
}


class 공덕식당저장소 extends 식당저장소 {
  // ...
}

object 공덕식당저장소 {
  val layer = ZLayer.succeed(new 공덕식당저장소)
}
```
위와 같이 클래스가 선언되었을 때 ZIO.service와 ZIOEffect의 R 타입은 아래와 같이 올 수 있습니다.
```scala

// ZIO[식당저장소, IOException, Unit]에서 식당저장소 대신 공덕식당저장소가 올 수 있습니다.
// ex) def useCaseUsingZLayer(): ZIO[공덕식당저장소, IOException, Unit] = for { ... }
def useCaseUsingZLayer(): ZIO[식당저장소, IOException, Unit] = for {
  repo <- ZIO.service[식당저장소]
  // ...
} yield ()


override def run = for {  
  _ <- ZIO.unit
  _ <- useCaseUsingZLayer().provideLayer(공덕식당저장소.layer)
} yield ()
```

ZIO.service[T]에 대해 자세히 알고 싶다면 [링크](https://zio.dev/reference/contextual/#accessing-zio-environment)(Accessing ZIO Environment)를 참고하세요.
