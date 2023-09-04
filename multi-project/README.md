# 멀티 프로젝트
## 개요
본 프로젝트는 build.[sbt](https://www.scala-sbt.org/)를 통해 멀티 프로젝트를 관리하는 방법에 대해 설명합니다.
중복되는 코드들을 성격별로 한 프로젝트에 모아서 여러 프로젝트에 의존하는 프로젝트를 만들 수 있습니다.

[공식 문서](https://www.scala-sbt.org/1.x/docs/Combined+Pages.html#Classpath+dependencies)

## 프로젝트 생성하기
중복되는 코드를 분리할 프로젝트를 생성하고 필요한 라이브러리를 추가합니다.
```sbt
lazy val `core-project` = project
  .settings(
    libraryDependencies ++= Seq(
      dev.zio" %% "zio" % zioVersion
    )
```

## 기존 프로젝트에 의존성 추가하기
기존 프로젝트가 분리된 코드를 사용할 수 있도록 분리한 프로젝트에 대한 의존성을 추가합니다.
```sbt
lazy val `main-project` = project
  .dependsOn(`core-project`)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio" %% "zio" % zioVersion
    )
```

## 여러 프로젝트에 의존하기
추가해야 할 프로젝트가 2개 이상인 경우 연달아 dependsOn을 작성함으로써 여러 프로젝트에 대한 의존성을 추가할 수 있습니다.
```sbt
lazy val `main-project` = project
  .dependsOn(`core-project`)
  .dependsOn(`sub-project`)
  .settings(
    libraryDependencies ++= Seq(
      dev.zio" %% "zio" % zioVersion
    )
```
