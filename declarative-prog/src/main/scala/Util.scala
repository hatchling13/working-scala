import zio._
import java.io.IOException

object Util {
  def readLine(message: String): IO[IOException, String] =
    zio.Console.readLine(message)
}
