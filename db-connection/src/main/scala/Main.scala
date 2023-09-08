import zio._
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}


object Main extends ZIOAppDefault {
  def run = {
    for {
      database <- new DBConnection().run

      dbService = new DBService()

      // TABLE CREATE
      _ <- dbService.createTable(database, "Person")

      // INSERT
      _ <- dbService.insertTableRow(database, TestTableRow("John", "Skiing"))
      _ <- dbService.insertTableRow(database, TestTableRow("Tom", "Skiing"))

      // SELECT
      rows <- dbService.selectTableRow(database)
      _ <- zio.Console.printLine(rows) // 조회 데이터 출력

      // UPDATE
      _ <- dbService.updateTableRow(database, TestTableRow("John", "sleep"))

      // SELECT
      rows <- dbService.selectTableRow(database)
      _ <- zio.Console.printLine(rows) // 조회 데이터 출력

      // DELETE
      _ <- dbService.deleteTableRow(database, "John")

      // SELECT
      rows <- dbService.selectTableRow(database)
      _ <- zio.Console.printLine(rows) // 조회 데이터 출력
    } yield ()
  }
}
