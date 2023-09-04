# 의존성 주입(Dependency Injection)

## 개요
예제를 통해 ZIO 환경에서 의존성 주입에 대해 설명한다.
일반적인 의존성 주입 방식부터 ZIO를 활용한 방식까지 다루는 예제이다.

- [ZIO 의존성 주입 공식 문서](https://zio.dev/reference/di/)
- [의존성 주입(DIExam.scala)](https://github.com/SHSongs/working-scala/blob/main/dependency-injection/src/main/scala/DiTest1.scala)
- [ZIO 라이브러리를 활용한 의존성 주입(DIExam2.scala)](https://github.com/SHSongs/working-scala/blob/main/dependency-injection/src/main/scala/DiTest2.scala)

## 의존성
```scala
"dev.zio" %% "zio" % zioVersion
```

## 메서드 목록
`ZLayer.succeed()`<br/>
값이나 서비스를 포함하는 레이어를 생성한다.
- [ZLayer 알아보기](https://zio.dev/reference/contextual/zlayer/)
```scala
object 판교식당저장소 {
    val layer = ZLayer.succeed(new 판교식당저장소)
}
```
판교식당저장소 인스턴스를 포함하는 레이어 생성


`provideLayer()`<br/>
ZIO ZLayer를 통해 의존성을 전달할 수 있도록 지원하는 메서드
```scala
def useCaseUsingZLayer(): ZIO[식당저장소, IOException, Unit] = for {
    // ...
} yield ()
```
의존성을 주입할 메서드 생성

```scala
_ <- useCaseUsingZLayer().provideLayer(판교식당저장소.layer)
```
useCaseUsingZLayer에 판교식당저장소 주입