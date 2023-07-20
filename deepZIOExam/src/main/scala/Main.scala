import zio._

import java.io.IOException

object Main extends ZIOAppDefault {
  override def run =
    for {
      start <- print("시작")  // 출력 : 시작 / start는 0 (print 함수 실행 후 반환 값)
      _ <- print(s"start는 ... $start")
      hello = print("안녕")   // 출력 : X / hello는 ZIO[Any, IOException, Int]
      _ <- print(s"hello는 ... $hello")
      x <- hello             // 출력 : 안녕 / hello는 0 (print 함수 실행 후 반환 값)
      _ <- print(s"x는 ... $x")
    } yield ()

  private def print(line: String): ZIO[Any, IOException, Int] = {
    zio.Console.printLine(line).as(0)
  }
}
