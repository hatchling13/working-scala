# 선언적 프로그래밍
## 개요
선언형 프로그래밍에 대한 예제코드를 작성한 프로젝트입니다.

## 의존성
```scala
lazy val `doobie-db` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
      "org.postgresql" % "postgresql" % "42.5.4",
    )
  )
```
**지난 deepZIO 모임에서 reporting을 했던 Friends의 age의 총합을 작성하는 코드를 작성해보세요.**

**PATH** deepZIOExam/src/main/scala/report/Reporting.scala

```scala
package report

import sttp.client3.ziojson.asJson
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend, UriContext, basicRequest}
import zio.json.DecoderOps
import zio.{ZIO, ZIOAppDefault}

object Reporting extends ZIOAppDefault {
  val path = os.pwd / "fixture"
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  val prog = for {
    _ <- ZIO.unit
    backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

    response = basicRequest
      .get(uri"http://localhost:13333/reporting-test")
      .response(asJson[Friend])
      .send(backend)

    f <- response.body match {
      case Left(_) => ZIO.fail(new Exception("fail"))
      case Right(friend) =>
        println(friend)
        ZIO.succeed(friend)
    }
  } yield f

  override def run = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / "friends.txt")))
      .catchAll(e => ZIO.fail(new Exception(s"Failed to parse json: $e")))

    jsonString = json.toString()
    eitherFriends = jsonString.fromJson[List[Friend]]

    friends <- ZIO.fromEither(eitherFriends)
    _ <- zio.Console.printLine(friends)
    ageSum = friends.map(_.age).sum
    _ <- zio.Console.printLine(s"나이 합계는 $ageSum")
  } yield ()
}
```

`**TODO` 좋은 취미(코딩, 요리하기)를 가진 Friends만 filter하는 코드를 작성해보세요**

**PATH** deepZIOExam/src/main/scala/report/Filtering.scala

```scala
case class HobbyFilter(hobby: String)
```

```scala
package report

import zio.json.DecoderOps
import zio.{ZIO, ZIOAppDefault}

object Filtering extends ZIOAppDefault {
  val path = os.pwd / "fixture"
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  def filterHobbies(hobbies: List[String], filters: List[HobbyFilter]): Boolean = {
    val hobbiesStr = hobbies.toString()
    filters.map(filter => hobbiesStr.contains(filter.hobby)).exists(x => x)
  }

  override def run = for {
    json <- ZIO
      .attempt(ujson.read(os.read(path / "friends.txt")))
      .catchAll(e => ZIO.fail(new Exception(s"Failed to parse json: $e")))

    eitherFriends = json.toString().fromJson[List[Friend]]
    friends <- ZIO.fromEither(eitherFriends)
    _ <- zio.Console.printLine(s"친구들 >>> $friends")

    filters = List(HobbyFilter("코딩"), HobbyFilter("요리하기"))
    goodFriends = friends
                .filter(f => filterHobbies(f.hobbies, filters))
                .map(f => GoodFriend(f.name, f.age))
    _ <- zio.Console.printLine(s"좋은 친구들 >>> $goodFriends")
  } yield ()
}
```

특정 파일의 위치에서 데이터를 읽어오는 serverExample, 데이터를 얻기 위해 서버로 요청을 보내는 ClientExample, 읽어온 데이터를 리포팅하는 Reporting.scala, 리포팅 된 데이터를 특정 기준에 따라 filter하는 Filtering.scala 는 각각의 역할이 분리되어 있습니다.

### 2. 선언적 인코딩(Declarative Encoding)

> [**Functional Design](https://degoes.net/articles/functional-design)의 Executable Encoding, Declarative Encoding 부분만 읽어보세요.**
> 

구체적으로 각 함수가 어떻게 구현 되었는지 세부사항을 이해할 필요 없이, 구체적으로 어떻게 실행될 것인지 계획(또는 명령)만 보고도 흐름을 쉽게 이해할 수 있습니다.

```scala
And(
// discount 또는 clearance를 포함하고 있거나, liquidation을 포함하지 않는 조건으로 filter합니다.
  Or(SubjectContains("discount"), SubjectContains("clearance")),
  Not(SubjectContains("liquidation"))
)
```

```scala
def matches(filter: EmailFilter. email: Email): Boolean = 
  filter match {
    case And(l, r) => matches(l, email) && matches(r, email)
    case Or(l, r) => matches(l, email) || matches(r, email)
    case Not(v) => !matches(v, email)
    case SubjectContains(phrase) => email.subject.contains(phrase)
  }
```

변할 수 있는 부분(ex. input)을 추상화 시켰을 때 변경에 유연하고, 실행과 계획을 명시적으로 분리할 수 있습니다.


## [예제1] 디스코드에 메시지 발송 기능

```scala
입력값이 5 이상일 때 디스코드 채널에 메시지를 보내는 기능을 추가해주세요.
```

아래 코드는 입력값이 5 이상일 때 디스코드 채널에 메시지를 보내는 예시입니다.

먼저 해당 기능을 구현하기 위해서는 다음과 같은 로직이 추가되어야 합니다.

1. 입력값이 5 이상인지 판별하는 함수
2. 디스코드 메시지를 생성하는 함수
3. 디스코드 메시지를 보내는 함수

```scala
// 함수 isSendableScore는 5이상의 값이 입력되었을 때 메시지를 생성합니다.
def isSendableScore(targetMood: Mood) = 
    targetMood.score match {
      case s if s >= 5 => Some(createMessage(Mood(targetMood.name, targetMood.score)))
      case _ => None

}
```

```scala
def createMessage(targetMood: Mood) = s"오늘의 기분은 ${targetMood.score}점이에요!"
```

```scala
// 메시지의 내용을 파라미터로 받아 디스코드 채널에 메시지를 보내는 함수
def sendDiscordMessage(message: String) = {
    val ZIOserviceKey = getEnvVariable("serviceKey")
    val ZIOwebhookKey = getEnvVariable("webhookKey")
    val requestPayLoad = {
      s"""
  {"content":"${message}","embeds":null,"username":"서브웨이 팀 봇","attachments":[]}
  """.stripMargin
    }

    val backend = HttpClientSyncBackend()

    for {
      webhookKey <- ZIOwebhookKey.flatMap(x =>
        x.flatMap(y => ZIO.fromOption(y))
      )
      uri = UriInterpolator.interpolate(
        StringContext(s"https://discord.com/api/webhooks/${webhookKey}")
      )
      _ <- Console.printLine(s"${uri}")
      _ <- ZIO.attempt(
        basicRequest
          .body(requestPayLoad)
          .header("Content-Type", "application/json", replaceExisting = true)
          .post(uri)
          .send(backend)
      )
    } yield ()
  }
```

추상화의 수준이 높아질수록 구현의 구체적인 내용은 알지 않아도 됩니다.

디스코드 메시지를 전달하기 위해 할 일은 함수를 호출하고 메시지 내용을 파라미터로 전달하는 것입니다. 

```scala
def addTodayMood() = for {
    _ <- Console.printLine("""
    오늘의 기분을 점수로 입력해주세요
    _________________________________
    0점 : BAD
    5점 : SOSO
    10점 : GOOD                   
    """)

    inputScore <- Console.readLine("점수")

    targetMood <- parseInsertInput(inputScore)
		// isSendableScore는 입력받은 값이 5 이상인 지 판별하고, 5 이상이면 메시지를 생성합니다.
    msg <- ZIO.fromOption(isSendableScore(targetMood))
		// sendDiscordMessage는 디스코드에 메시지를 전송합니다.
    _ = sendDiscordMessage(msg)

    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          insertMood(targetMood.name, targetMood.score)
        }
      } yield res)

    _ <- zio.Console.printLine("입력 완료되었습니다.")
    _ <- zio.Console.printLine(rows)

  } yield ()
```

---

## [예제2] 쿠폰 발급하기

[관련코드] https://github.com/SHSongs/working-scala/pull/23

<aside>
<img src="/icons/user-circle-filled_red.svg" alt="/icons/user-circle-filled_red.svg" width="40px" /> **추가 준비물 
2. 쿠폰 발급하기**
- DB에 user table을 만들고 string type의 name과 int type의 level column을 만드세요.
- coupon table을 만들고 string type의 owner와 int type의 discount column을 만드세요. (owner는 user table의 name의 외래키입니다)
- user table과  데이터를 넣고 해당 유저가 7 level이 넘는다면 해당 유저에게 discount 100 쿠폰을 발급해주세요.

</aside>

---

### [참고] ZIO의 Error 종류

`**Failures`는 예상 가능한 에러(expected error)입니다. failure를 핸들링하기 위해 `ZIO.fail`을 사용합니다. failure는 콜 스택 전체에 걸쳐 전파되지 않도록 해야 합니다.** failure는 스칼라 컴파일러의 도움을 받아 타입 시스템으로 밀어 넣음으로써 처리합니다. ZIO에서는 E라는 Error Type 매개변수가 있으며, 이 Error Type 매개변수는 애플리케이션 내의 모든 예상되는 오류를 모델링하는 데 사용됩니다.

`Defects`은 예상치 못한 오류(unexpected error)입니다. defects를 핸들링하기 위해 `ZIO.die`를 사용합니다. 애플리케이션 스택을 통해 defects를 전파(propagate)해야 합니다. 

- 상위 레이어 중에서 defect를 예상하는 것이 의미가 있을 경우, 이를 Failure로 변환하고 핸들링합니다.
- 상위 레이어 중에서 catch하지 않는다면 어플리케이션 전체에 crash가 나게 만듭니다.

`Fatals`은 치명적이고 예상치 못한 오류입니다. 이러한 오류가 발생할 때는 해당 오류를 더 이상 전파하지 않고 즉시 어플리케이션을 종료해야 합니다. 오류를 로깅하고 콜스택을 출력할 수 있습니다.

[Typed Errors Guarantees | ZIO](https://zio.dev/reference/error-management/typed-errors-guarantees/)





### 메소드 목록

### 참고문서

- https://zio.dev/reference/error-management/imperative-vs-declarative/
- https://degoes.net/articles/functional-design