import zio._
import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.Database

object Damaged extends ZIOAppDefault {
  def getUser(id: Int) = for {
    users <- PostgreSQLService
      .selectFromTable[User]("user_table", List("id", "name", "hp"))

    result = users match {
      case Left(errorMessage) => Left(errorMessage)
      case Right(users) =>
        users.find(user => user.id == id) match {
          case None       => Left("No such user!")
          case Some(user) => Right(user)
        }
    }
  } yield result

  def subtractHp(user: Either[String, User], value: Int) = for {
    _ <- ZIO.unit

    result <- user match {
      case Left(errorMessage) =>
        for {
          _ <- ZIO.unit
        } yield Left(errorMessage)
      case Right(user) =>
        for {
          _ <- ZIO.unit

          afterHp = user.hp - value

          subtractResult = afterHp > 100 || afterHp < 0 match {
            case true  => Left("Invalid HP!")
            case false => Right(User(user.id, user.name, afterHp))
          }
        } yield subtractResult
    }
  } yield result

  def attackUser(id: Int, damage: Int) = for {
    retrieveduser <- getUser(id)

    subtractResult <- subtractHp(retrieveduser, 10)

    updateResult <- PostgreSQLService.updateUser(subtractResult)
  } yield updateResult

  private val program = for {
    doesTableExists <- PostgreSQLService.userTableExists("user_table")

    createTableResult <- PostgreSQLService.createUserTable(doesTableExists)

    _ <- createTableResult match {
      case Left(message) =>
        Console
          .printLine(message)
          .catchAll(ioException => ZIO.ignore(ioException))
      case Right(_) => ZIO.unit
    }

    _ <- Console
      .printLine("Attacking user!")
      .catchAll(error => ZIO.ignore(error))

    // Not covered: when no such user
    result <- attackUser(1, 10)

    _ <- result match {
      case Left(error) =>
        Console
          .printLineError(error)
          .catchAll(ioException => ZIO.ignore(ioException))
      case Right(value) =>
        Console
          .printLine(value)
          .catchAll(ioException => ZIO.ignore(ioException))
    }

  } yield ()
  override def run = program.provide(
    PostgreSQLService.DBLayer
  )
}
