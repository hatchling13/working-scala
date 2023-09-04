import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

import java.io.IOException

object Main extends ZIOAppDefault {

  def choiceMenu(): ZIO[Any, IOException, String] = for {
    _ <- zio.Console.printLine("1. TestConsole")
    _ <- zio.Console.printLine("2. TestClock")
    _ <- zio.Console.printLine("3. TestRandom")
    choice <- zio.Console.readLine("실행할 예제를 선택하세요: ")
  } yield choice

  override def run = for {
    input <- choiceMenu()
    _ <- input match {
      case "1" => InputUserInfo.run()
      case "2" => LazyTurtle.run()
      case "3" => ZombieGame.run()
      case _ => zio.Console.printLine("잘못된 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()
}
