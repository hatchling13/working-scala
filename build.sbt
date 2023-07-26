ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

val zioVersion = "2.0.13"
val sttpVersion = "3.7.4"
val sttpClientVersion = "3.7.4"

lazy val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % zioVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "working-scala"
  )

lazy val `zio-json` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % "0.3.0-RC10"
    )
  )

lazy val `tapir-with-zio-http` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "io.d11" %% "zhttp" % "2.0.0-RC10",
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % "1.0.4",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.0.4"
    )
  )

lazy val `http-server` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2"
    )
  )

lazy val `http-client` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "dev.zio" %% "zio-json" % "0.6.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio" % "3.8.16" // for ZIO 2.x
    )
  )

lazy val `read-file` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "dev.zio" %% "zio-json" % "0.3.0-RC10"
    )
  )

lazy val `modeling-youtube` = project.settings(sharedSettings)

lazy val `cheese` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2"
    )
  )

lazy val `deepZIOExam` = project
  .settings(sharedSettings)
  .settings(
  )

lazy val Forecast = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "dev.zio" %% "zio-json" % "0.3.0-RC10",
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M2",
      "com.softwaremill.sttp.client4" %% "zio-json" % "4.0.0-M2",
      "com.nrinaudo" %% "kantan.csv" % "0.7.0",
    )
  )

lazy val `forecast-cheese` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.8.16",
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-json" % "0.5.0"
    )
  )

lazy val `simple-jh` = project
  .settings(sharedSettings)

lazy val `forecast` = project.dependsOn(`read-file`)
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "com.lihaoyi" %% "ujson" % "3.0.0",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.16",
      "com.softwaremill.sttp.client3" %% "zio-json" % "3.3.9",
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "org.scalameta" %% "scalafmt-core" % "2.7.5"
    )
  )