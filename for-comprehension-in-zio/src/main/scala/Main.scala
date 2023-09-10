import zio._

object Main extends ZIOAppDefault {
  val getTheAnswer = ZIO.succeed(42)

  val effectWithFor = for {
    answer <- getTheAnswer
    answerText = answer.toString()
    _ <- Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answerText!")
  } yield ()

  val effectWithHOF = getTheAnswer
    .map(answer => answer.toString())
    .flatMap(answerText =>
      Console.printLine(s"삶, 우주 그리고 모든 것에 대한 답은 $answerText!")
    )

  private val program = for {
    _ <- effectWithFor
    _ <- effectWithHOF
  } yield ()

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program
}
