import zio.ZIO
import java.time.LocalDateTime

object FileManager {
  val PATH = os.pwd / "file-handling" / "files"

  def read(fileName: String) = for {
    data <- ZIO
      .attempt(os.read(PATH / fileName))
      .catchAll(cause => ZIO.fail(new RuntimeException(s"${fileName} 파일 읽기에 실패했습니다. (${cause})")))
  } yield data

  def write() = for {
    _ <- ZIO.unit
    now = LocalDateTime.now()
    fileName = s"${now}.txt"
    contents = s"[${now}.txt] 예제를 실행했습니다."
    _ <- ZIO
      .attempt(os.write(PATH / fileName, contents))
      .catchAll(cause => ZIO.fail(new RuntimeException(s"${fileName} 파일 쓰기에 실패했습니다. (${cause}")))
    _ <- zio.Console.printLine(s"파일을 생성했습니다. (생성된 파일명 : ${fileName})")
  } yield ()

  def overwrite(fileName: String, contents: String) = for {
    _ <- ZIO
      .attempt(os.write.over(PATH / fileName, contents))
      .catchAll(cause => ZIO.fail(new RuntimeException(s"${fileName} 파일 쓰기에 실패했습니다. (${cause}")))
    _ <- zio.Console.printLine(s"파일을 생성했습니다. (생성된 파일명 : ${fileName})")
  } yield ()
}
