import zio._
import zio.json._
import ujson.Value.Value
import java.nio.file.NoSuchFileException


object Main extends ZIOAppDefault {
  def run = {
    for {
      // Either
      fail <- zeither
      _ <- zio.Console.printLine(fail)

      // Catch All
      json <- ZIO.attempt(ujson.read(os.read(os.pwd / "primary.json"))).catchAll(error => 
        for {
          _ <- zio.Console.printLine(s"error >> $error")
        } yield()
      )
      
      // Catch Some
      json <- ZIO.attempt(ujson.read(os.read(os.pwd / "primary2.json"))).catchSome{
        case _ : NoSuchFileException => zio.Console.printLine("[Error] File not found.")
      }

      // Fallback
      json <- ZIO.attempt(ujson.read(os.read(os.pwd / "primary.json"))).orElse(zio.Console.printLine("It is Fallback Exmaple."))

      // Folding
      json <- ZIO.attempt(ujson.read(os.read(os.pwd / "primary.json"))).foldZIO(
        _ => zio.Console.printLine("[Error] Folding Example"),
        data => ZIO.succeed(data)
      )

      // Retrying
      result <- ZIO.attempt(ujson.read(os.read(os.pwd / "primary.json"))).retryOrElse(
        Schedule.recurs(5), (error, output: Long) => ZIO.succeed(s"[Error] $error $output")
        )
      _ <- zio.Console.printLine(result)
    } yield()
  }

  val zeither: ZIO[Any, Nothing, Either[String, Nothing]] = 
    ZIO.fail("Uh oh!").either
}