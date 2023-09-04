import zio._
import zio.Console


object Main extends ZIOAppDefault {

  def run = {
    for {
      _ <- Console.printLine("Dependency Injection START")
      _ <- DITest1.run // List(김밥집, 피자집, 샌드위치집)
      _ <- DITest2.run // List(카카오국밥, 네이버국밥, 엔씨국밥)
      _ <- Console.printLine("Dependency Injection END")
    } yield ()
  }
}