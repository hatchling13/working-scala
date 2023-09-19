import zio._

object Main extends ZIOAppDefault {

  override def run = for {
    input <- choiceMenu()
    _ <- input match {
      case "1" => FileManager.read("readonly.txt").debug("# 실행 결과 => ")
      case "2" => FileManager.write()
      case "3" => fileCustom()
    }
  } yield ()

  def choiceMenu() = for {
    _ <- zio.Console.printLine("1. 파일 읽기(readonly.txt)")
    _ <- zio.Console.printLine("2. 파일 쓰기")
    _ <- zio.Console.printLine("3. 파일 직접 만들기")
    choice <- zio.Console.readLine("실행할 예제를 선택하세요: ")
  } yield choice

  def fileCustom() = for {
    _ <- zio.Console.printLine("파일명과 파일 내용을 입력하면 txt 파일이 생성됩니다. 동일한 파일명이 존재하면 덮어씁니다.")
    fileName <- zio.Console.readLine("파일명을 입력하세요(확장자 제외) : ")
    contents <- zio.Console.readLine("파일 내용을 입력하세요 : ")
    _ <- FileManager.overwrite(s"${fileName}.txt", contents)
  } yield ()

}
