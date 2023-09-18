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

lazy val `json-handling` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-json" % "0.3.0-RC10"
    )
  )

lazy val `bicycle_db` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.postgresql" % "postgresql" % "42.5.4",
      "dev.zio" %% "zio-http" % "3.0.0-RC2"
    )
  )

lazy val `declarative-prog` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.0.0-RC2",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.xerial" % "sqlite-jdbc" % "3.40.1.0",
      "org.postgresql" % "postgresql" % "42.5.4"
    )
  )

lazy val `dependency-injection` = project
  .settings(sharedSettings)

lazy val `for-comprehension-in-zio` = project
  .settings(sharedSettings)
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val `for-comprehension-in-scala` = project
  .settings(sharedSettings)
  .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val `db-connection` = project
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

lazy val `multi-project` = project
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
    )
  )

lazy val `testing` = project
  .settings(sharedSettings)

lazy val `error-handling` = project
  .settings(sharedSettings)
  .settings(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "os-lib" % "0.9.1",
    "com.lihaoyi" %% "ujson" % "3.0.0",
    "dev.zio" %% "zio-json" % "0.3.0-RC10"
  )
)
