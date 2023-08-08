import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._

object DBConnection {
  val postgres = locally {
    val path = "localhost:5433"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  val driver = "org.postgresql.Driver"

  Class.forName(driver)

  val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}
