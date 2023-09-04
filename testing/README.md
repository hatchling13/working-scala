# 테스팅
## 개요
본 프로젝트는 테스트 코드를 보다 효과적으로 작성하기 위해 ZIO가 제공하는 기능에 대해 실습합니다.

[공식 문서](https://zio.dev/reference/test/services/)

## 의존성
ZIO의 test 패키지를 사용하려면 아래와 같은 의존성을 추가합니다.
```sbt
"dev.zio" %% "zio" % zioVersion,
"dev.zio" %% "zio-test" % zioVersion % Test,
"dev.zio" %% "zio-test-sbt" % zioVersion % Test
```

## 메소드 목록
### TestConsole
- feedLines(lines: String*): UIO[Unit]
  - readLine에 문자열(lines)을 공급합니다.
- output: UIO[Vector[String]]
  - printLine으로 만들어진 내용을 가져옵니다.
### TestClock
- adjust(duration: => Duration): UIO[Unit]
  - duration만큼 시간을 조절합니다.
### TestRandom
- feedInts(ints: Int*): UIO[Unit]
  - Random.nextInt()에 정수형 데이터(ints)를 공급합니다.
