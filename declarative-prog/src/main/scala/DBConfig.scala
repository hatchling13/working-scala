import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import java.sql.{Connection, DriverManager, SQLException}


trait DBService {
  def connection: ZLayer[Any, SQLException, Connection]

  lazy val DBLayer =
    connection >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
}

object Postgres extends DBService  {
  val config = DBConfig(
    Host("localhost", "postgres", 5433),
    Login("postgres", "1q2w3e4r")
  )

  val url = s"jdbc:postgresql://${config.host.path}:${config.host.port}/${config.host.name}?user=${config.login.user}&password=${config.login.password}"

  val driver = "org.postgresql.Driver"

  Class.forName(driver)
  def connection = for {
    layer <- ZLayer(ZIO.attempt(DriverManager.getConnection(url)))
      .mapError(error => new SQLException(error))
  } yield layer
}

case class Host(path: String, name: String, port: Int)
case class Login(user: String, password: String)

case class DBConfig(
    host: Host,
    login: Login
)
