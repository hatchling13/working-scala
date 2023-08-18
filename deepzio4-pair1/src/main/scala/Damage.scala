import zio._
import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.Database

object Damaged extends ZIOAppDefault {
  private val conn = PostgreSQLConnection.connection

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
    retrieveduser <- PostgreSQLConnection.getUser(id)

    subtractResult <- subtractHp(retrieveduser, 10)

    updateResult <- PostgreSQLConnection.updateUser(subtractResult)
  } yield updateResult

  private val program = for {
    doesTableExists <- PostgreSQLConnection.userTableExists()

    createTableResult <- PostgreSQLConnection.createUserTable(doesTableExists)

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
    conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
  )
}
