import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import doobie.implicits._
import doobie._
import zio._
import zio.{Console}
import DiaryRepository._
import MoodParser._
import sttp.client3._
import sttp.model.UriInterpolator

object DiaryService {
  def isSendableScore(targetMood: Mood) = 
    targetMood.score match {
      case s if s >= 5 => Some(createMessage(Mood(targetMood.name, targetMood.score)))
      case _ => None

}
def getEnvVariable(name: String) = {
    val parsedName = name match {
      case "serviceKey" => ZIO.succeed(name)
      case "webhookKey" => ZIO.succeed(name)
      case _            => ZIO.fail(name)
    }
    parsedName.map(validName => System.property(validName))
  }

  def createMessage(targetMood: Mood) = s"오늘의 기분은 ${targetMood.score}점이에요!"
  def sendDiscordMessage(message: String) = {
    val ZIOserviceKey = getEnvVariable("serviceKey")
    val ZIOwebhookKey = getEnvVariable("webhookKey")
    val requestPayLoad = {
      s"""
  {"content":"${message}","embeds":null,"username":"서브웨이 팀 봇","attachments":[]}
  """.stripMargin
    }

    val backend = HttpClientSyncBackend()

    for {
      webhookKey <- ZIOwebhookKey.flatMap(x =>
        x.flatMap(y => ZIO.fromOption(y))
      )
      uri = UriInterpolator.interpolate(
        StringContext(s"https://discord.com/api/webhooks/${webhookKey}")
      )
      _ <- Console.printLine(s"${uri}")
      _ <- ZIO.attempt(
        basicRequest
          .body(requestPayLoad)
          .header("Content-Type", "application/json", replaceExisting = true)
          .post(uri)
          .send(backend)
      )
    } yield ()
  }

  def getAllMoods = for {
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          getAll
        }
      } yield res)
    _ <- Console.printLine("""
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
    _ <- zio.Console.printLine(rows)
  } yield ()

  def addTodayMood = for {
    _ <- Console.printLine("""
    오늘의 기분을 점수로 입력해주세요
    _________________________________
    0점 : BAD
    5점 : SOSO
    10점 : GOOD                   
    """)

    inputScore <- Console.readLine("점수")
    targetMood <- parseInsertInput(inputScore)
		// isSendableScore는 입력받은 값이 5 이상인 지 판별하고, 5 이상이면 메시지를 생성합니다.
    msg <- ZIO.fromOption(isSendableScore(targetMood))
		// sendDiscordMessage는 디스코드에 메시지를 전송합니다.
    _ = sendDiscordMessage(msg)

    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          insertMood(targetMood.name, targetMood.score)
        }
      } yield res)

    _ <- zio.Console.printLine("입력 완료되었습니다.")
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
    _ <- Console.printLine("""
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
    _ <- zio.Console.printLine(rows)
    inputNumber <- Console.readLine("수정할 항목을 번호로 입력해주세요 : ")
    inputScore <- Console.readLine("수정할 점수를 입력해주세요 : ")
    targetNumber = parseUpdateInput(inputNumber)
    targetMood <- parseInsertInput(inputScore)
    updatedRow <- database.transactionOrWiden(for {
      res <- tzio {
        updateOne(targetNumber, targetMood)
      }
    } yield res)

    _ <- zio.Console.printLine("수정이 완료되었습니다.")

  } yield ()
  def deleteMood = for {
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          getAll
        }
      } yield res)
    _ <- Console.printLine("""
        지금까지 기록한 Mood 목록입니다.
        _________________________________              
        """)
    _ <- zio.Console.printLine(rows)
    inputNumber <- Console.readLine("삭제할 항목을 번호로 입력해주세요 : ")
    targetNumber = parseUpdateInput(inputNumber)
    updatedRow <- database.transactionOrWiden(for {
      res <- tzio {
        deleteOne(targetNumber)
      }
    } yield res)
    _ <- zio.Console.printLine(s"${targetNumber}번은 삭제되었습니다.")
  } yield ()

}
