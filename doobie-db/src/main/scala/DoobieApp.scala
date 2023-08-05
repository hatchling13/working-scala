import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

// 1 . datagrip을 통해서 table setting
// 2. 간단한 update, delete query를 날리는 로직을 작성한다.
// 3. 유저의 입력은 readLine으로 받아서 변수에 담음
// 4. ${변수명} 쿼리
// 5. CRUD 구현

case class Mood(name: String, score: Int)

object DoobieApp extends ZIOAppDefault {
    def validateInput(score: Int) =
        score match {
        case 10 => Mood("GOOD", 10)
        case 5 => Mood("SOSO", 5)
        case 0 => Mood("BAD", 0)
        case _ => Mood("NONE", -1)
  }


  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|select name, score
                |from "DailyNotes".mood""".stripMargin
            .query[Mood]
            .to[List]
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

  } yield ()

  // def insert1(name: String, age: Option[Short]) =
  // sql"insert into person (name, age) values ($name, $age)".update.run

  override def run = prog.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

  // val sqlite = locally {
  //   val path = "identifier.sqlite"
  //   s"jdbc:sqlite:$path"
  // }
  val postgres = locally {
    val path = "localhost:5433"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  val driver = "org.postgresql.Driver"

  Class.forName(driver)

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}