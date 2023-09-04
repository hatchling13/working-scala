import zio._

object LazyTurtle {
  def run() = for {
    _ <- ZIO.unit
    turtle = Turtle()
    _ <- ZIO.foreach(1 to 10) { _ => turtle.move() }
    _ <- zio.Console.printLine(s"다 움직였어요! 거북이의 위치 ${turtle.position}mm")
  } yield turtle

  case class Turtle() {
    var position = 0

    def move() = for {
      _ <- ZIO.unit
      _ = position += 1
      _ <- ZIO.sleep(1.seconds)
      _ <- zio.Console.printLine("엉금엉금 1mm 움직이는중 ...")
    } yield ()
  }
}
