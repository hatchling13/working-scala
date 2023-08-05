import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie.implicits._
import doobie._
import zio._
import zio.{Console}
import SQLRepository._
import MoodParser._

object DiaryService {

    def getAllMoods = for {
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
            getAll
        }
      } yield res)
        _ <- Console.printLine(
        """
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
        _ <- zio.Console.printLine(rows)
    } yield ()

  def addTodayMood = for {
    _ <- Console.printLine(
    """
    오늘의 기분을 점수로 입력해주세요
    _________________________________
    0점 : BAD
    5점 : SOSO
    10점 : GOOD                   
    """)

    inputScore <- Console.readLine("점수")
    mood = parseInsertInput(inputScore)

    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(
      for {
        res <- tzio {
          deleteAllMood()
          insertMood(mood.name, mood.score)
          getAll()
        }
      } yield res)

    _ <- zio.Console.printLine(rows)

  } yield ()

def modifyMood = for {
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
            getAll
        }
      } yield res)
        _ <- Console.printLine(
        """
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
        _ <- zio.Console.printLine(rows)
        inputNumber <- Console.readLine("수정할 항목을 번호로 입력해주세요 : ")
        inputScore <- Console.readLine("수정할 점수를 입력해주세요 : ")
        targetNumber = parseUpdateInput(inputNumber)
        targetScore = parseUpdateInput(inputScore)
        updatedRow <- database.transactionOrWiden(for {
            res <-tzio {
               updateOne(targetNumber,targetScore)
            }
        }
        yield res)

        _ <- zio.Console.printLine(updatedRow)

  } yield ()
  def deleteMood = for {
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
            getAll
        }
      } yield res)
        _ <- Console.printLine(
        """
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
        _ <- zio.Console.printLine(rows)
        inputNumber <- Console.readLine("삭제할 항목을 번호로 입력해주세요 : ")
        targetNumber = parseUpdateInput(inputNumber)
        updatedRow <- database.transactionOrWiden(for {
            res <-tzio {
               deleteOne(targetNumber)
            }
        }
        yield res)
        _ <- zio.Console.printLine(s"${targetNumber}번은 삭제되었습니다.")        
  } yield ()

}
