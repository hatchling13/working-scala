import zio._

object Main extends ZIOAppDefault {
  val getTheAnswer = ZIO.succeed(42)

  private val effect1 = getTheAnswer
    .flatMap(answer => Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answer!"))

  private val effect2 = for {
    answer <- getTheAnswer
    _ <- Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answer!")
  } yield ()

  private val program = for {
    _ <- Console.printLine(
      "어떠한 형태의 코드를 실행할까요? (1: flatMap 형태, 2: for-comprehension 형태)"
    )

    input <- Console.readLine("입력 : ")

    _ <- input match {
      case "1" => effect1
      case "2" => effect2
      case _   => Console.printLineError("올바르지 않은 입력입니다. 프로그램을 종료합니다.")
    }
  } yield ()

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program
}
