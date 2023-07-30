import com.bicycle_db.User
import com.bicycle_db.RentalRecord
import com.bicycle_db.Station
import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._
import org.checkerframework.checker.units.qual.s
import com.bicycle_db.BikeRentalService

object DBSampleApp extends ZIOAppDefault {
  // 쿼리 보내고 
  def getStation(stationId: String): ZIO[Database, Throwable, Station] = {
    val query = tzio {
      sql"""
      |select id, name, available_bikes
      |from stations
      |where available_bikes > 0 = $stationId
      |""".stripMargin
        .query[Station]
        .unique
    }

    val db = ZIO.service[Database]
    db.flatMap(database => database.transactionOrWiden(query))
  }

  // 되냐?  

  def readLine = 
    for {
      _ <- zio.Console.printLine("Enter your bike id: ")
      id <- zio.Console.readLine
      _ <- zio.Console.printLine("Enter your password: ")
      password <- zio.Console.readLine
      // _ <- Console.printLine(s"Your id: $id, password: $password")
      stationId <- zio.Console.readLine
      _ <- Console.printLine(s"Your id: $id, password: $password, stationId: $stationId")
    } yield (id, password, stationId)

  override def run = {
    // 먼저 `readLine`에지 각 정보들을 가져와야 됨
    // 이후 가져온 정보에서 `stationID`를 가져오고 getStation에 넘져줘야 됨
    // 나머지 정보들을 User에 넘겨줘야 됨

    for{
      a <- readLine
      (id, password, stationId) = a
      b = getStation(stationId)
      _ <- Console.printLine(b)
      // user = User.findUser(id)
      // record = RentalRecord.createRentalRecord(RentalRecord(id, stationId, 0, stationId, 0, 0))
    } yield ()
  }

  val sqlite = locally {
    val path = "identifier.sqlite"
    s"jdbc:sqlite:$path"
  }
  val postgres = locally {
    val path = "localhost:5430"
    val name = "postgres"
    val user = "postgres"
    val password = "1q2w3e4r"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}

