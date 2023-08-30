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


## [예제] 쿠폰 발급하기

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