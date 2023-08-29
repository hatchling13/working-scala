import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio._
import doobie._
import doobie.implicits._

import DBConnection._

case class TestTableRow(name: String, hobby: String)

object Main extends ZIOAppDefault {
  val main = for {
    // 내용
  } yield ()
  override def run = main.provide(
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )
}
