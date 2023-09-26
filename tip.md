## 문제 상황 : Console에 값이 출력되지 않아요
```scala
import zio._  		  

object ZIOApp extends ZIOAppDefault {
	val effect: ZIO[Any, Nothing, Int] = { ZIO.succeed(3).map(_ * 2) }

	override def run = for {
		_ <- zio.Console.printLine(s"effect : ${effect}")
	}
}

```
디버깅을 위해 콘솔에 값을 출력하는 상황이라고 가정했을 때 6이 출력되길 기대합니다.

```terminal
effect : OnSuccess(<empty>.Main.effect(Main.scala:6),Sync(<empty>.Main.effect(Main.scala:6),Main$$$Lambda$6762/0x000000e0022b49f0@614a9f2c),zio.ZIO$$Lambda$6764/0x000000e0022b5090@6f8d7d75)
```
하지만 이러한 값이 나온다면 ZIO type\*으로 감싸져 있는 경우를 의심해 볼 수 있습니다.

### - ZIO type을 반환하는 메서드
|Function|Input Type|Output Type|Note|
|---|---|---|---|
|`succeed`|`A`|`UIO[A]`|Imports a total synchronous effect|
|`attempt`|`A`|Task[A]|Imports a (partial) synchronous side-effect|

> ZIO.succeed 메서드를 사용하면 특정한 값에 대해 동기적인 효과((synchrouonous Effect)를 생성할 수 있습니다. 실패(Failure)하는 경우는 고려하지 않습니다.

표를 보면, ZIO.succeed() 메서드의 반환타입은 UIO[A]와 같은 ZIO type입니다.

### \*ZIO type이란?
ZIO[R,E,A]를 기본 시그니처로 가지며, ZIO[Any, Throwable, A]과 같이 다양한 형태로 존재합니다.

- Task[+A]
```scala
type Task[+A] = ZIO[Any, Throwable, A]
```
- IO
```scala
type IO[+E, +A] = ZIO[Any, E, A]
```
- UIO[+A]
```scala
type UIO[+A] = ZIO[Any, Nothing, A]
```

## 왜 발생하나요?
아래 코드는 Data의 값을 비동기적으로 가져올 수 있는 경우, effect가 어떻게 생성되는 지 알아보기 위해 가져온 예시코드입니다.
```scala
// https://degoes.net/articles/zio-cats-effect

val effect:Task[Data] =
	Async[Task].async(k=>getDataWithCallbacks
						(onSuccess = v => k(Right(v)),
						 onFailure = e => k(Left(e)))
						 )
```
ZIO에서는 모든 동작을 계획과 타입으로 감싸기 때문에 모든 타입은 예상가능한 실패와 예상할 수 없는 실패 두 가지가 존재합니다.
예시처럼 비동기적으로 값을 가져온다고 했을 때 
1. 값을 가져오는 데 성공한 경우(onSuccess)
2. 값을 가져오는 데 실패하는 경우(onFailure)
가 존재하고 만약 값을 가져오는데 성공한 경우에 대해서만 실제 가져온 값을 반환하고, 실패했을 경우에는 에러를 반환하게 됩니다.
(값을 사용가능할 때까지 대기한 다음, 비동기 Effect를 실행합니다.)
실제 값이 출력되지 않는 이유는 ZIO type으로 감싸진 값이 아직 *실행되지 않았기 때문*입니다.

## 어떻게 해야 할까요?
> ZIO type에서 꺼내면 됩니다.
- for comprehension과 `<-`를 사용하기
```scala
import zio._


object Main extends ZIOAppDefault {
	val effect: ZIO[Any, Nothing, Int] = {
		ZIO.succeed(3).map(_ * 2)
	}

	override def run = for {
		_ <- zio.Console.printLine(s"effect : ${effect}")
		value <- effect
		_ <- zio.Console.printLine(s"after executing effect, value is : ${v}")
	} yield ()
	
}
```

- flatMap을 사용하기
```scala
import zio._

object Main extends ZIOAppDefault {

	val effect: ZIO[Any, Nothing, Int] = {
		ZIO.succeed(3).map(_ * 2)
	}

	def runFlat = {
		effect.flatMap(r => zio.Console.printLine(s"after executing Effect, value is : ${r}"))
	}

	override def run = for {
		_ <- zio.Console.printLine(s"effect : ${effect}")
		_ <- runFlat
	} yield ()

}
```




flatMap 또는 for comprehension 안의 `<-`을 통해서 UIO[+A]가 실행됨으로서 A를 꺼낼 수 있습니다.



이 동작에 대해 더 자세히 이해하고 싶으면 [Functional Effect System in Scala](https://medium.com/wix-engineering/demystifying-functional-effect-systems-in-scala-14419039a423)를 참조해주세요.


## 참고) flatMap의 구현체
```scala
//https://github.com/zio/zio/blob/57d92a9264fbe71c9b6cf18a07d588809f096e4a/core/shared/src/main/scala/zio/ZIO.scala#L959

def flatMap[R1 <: R, E1 >: E, B](k: A => ZIO[R1, E1, B])(implicit trace: Trace): ZIO[R1, E1, B] = 
ZIO.OnSuccess(trace, self, k)
```

## 참고 문서
[ZIO | ZIO#expected-and-unexpected-errors](https://zio.dev/reference/error-management/expected-and-unexpected-errors/)
[ZIO | ZIO#from-side-effects](https://zio.dev/reference/core/zio/#from-side-effects)
[Effect | error-management/matching](https://effect.website/docs/error-management/matching)
[ZIO & Cats Effect: A Match Made in Heaven](https://degoes.net/articles/zio-cats-effect)
[ZIO | ZIO#debug-a-zio-application](https://zio.dev/guides/tutorials/debug-a-zio-application/)
