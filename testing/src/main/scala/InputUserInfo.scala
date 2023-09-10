object InputUserInfo {
  def run() = for {
    name <- zio.Console.readLine("이름을 입력하세요: ")
    age <- zio.Console.readLine("나이를 입력하세요: ")
    _ <- zio.Console.printLine(s"${name}(${age}세)님 안녕하세요!")
  } yield User(name, Integer.parseInt(age))

  case class User(name: String, age: Int)
}
