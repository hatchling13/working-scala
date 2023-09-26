import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._

class DBConnection extends ZIOAppDefault  {
   override def run = prog.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )

  val prog = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
  } yield (database)

  val sqlite = locally {
    val path = "identifier.sqlite"
    s"jdbc:sqlite:$path"
  }

  val postgres = locally {
    val path = "localhost:5432"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}