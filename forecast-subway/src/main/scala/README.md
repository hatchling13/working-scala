

### build.sbt
```scala
lazy val `forecast` = project
  .settings(sharedSettings)
  .settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "ujson" % "3.0.0",
    "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
    "com.softwaremill.sttp.client3" %% "zio-json" % "3.8.16"  // for ZIO 2.x
  )
)
```

### 관련자료
- [정규모임2 사전 준비물 Notion](https://spiny-entree-7e0.notion.site/2-dd89e133b01547329a9462ab5456f6a5)
- [공공데이터포털 기상청 단기예보](https://www.data.go.kr/data/15084084/openapi.do)
  - 기상청 Open-API 문서(`./기상청_단기예보_조회서비스.pdf`)
  - Response 파일(`./response.body.csv`)
  
