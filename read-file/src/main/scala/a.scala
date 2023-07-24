import zio._

import java.io.IOException

object Main2 extends ZIOAppDefault {
  // flatmap과 map은 세 번째 타입인 Int를 반환한다.
  // 여기서 사용하는 flatmap은 ZIO에서 사용하는 라이브러리를 오버라이드 한 것
  def print(line: String): ZIO[Any, IOException, Int] =
    zio.Console.printLine(line).as(0)

  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")

    prog = for {
      one <- print("시작") // 일어난 일: "시작" 출력, one의 값은 0
      two = print("끝") //  일어난 일: 아무일도 일어나지 않음, two의 값은 ZIO[Any, IOException, Int]
      tow = print("시작").map(x => print("끝"))
      x <- tow // 일어난 일: "끝" 출력, two의 값은 0
    } yield ()

    _ <- prog

  } yield ()

  val a = List(1, 2, 3)
  val b = a.map(x => List(1, 2, 3))
  val flatb = List(1, 2, 3, 1, 2, 3, 1, 2, 3)
  // flatmap으로 값을 꺼내는 것을 <-로 표
}
