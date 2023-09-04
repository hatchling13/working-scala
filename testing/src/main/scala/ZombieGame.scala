import zio._

object ZombieGame {
  def run() = for {
    _ <- ZIO.unit
    random <- Random.nextInt
    _ <- random match {
      case 1 => zio.Console.printLine("도망 성공!")
      case _ => zio.Console.printLine("도망에 실패했습니다.")
    }
  } yield ()
}
