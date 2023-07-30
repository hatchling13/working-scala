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
      "com.lihaoyi" %% "ujson" % "3.0.0"
    )
  )

lazy val `read-file` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "os-lib" % "0.9.1",
      "com.lihaoyi" %% "ujson" % "3.0.0"
    )
  )

lazy val `sample-db-taste-review` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.postgresql" % "postgresql" % "42.5.4",
      "dev.zio" %% "zio-http" % "3.0.0-RC2"
    )
  )
