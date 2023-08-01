import doobie.implicits.toSqlInterpolator
import io.github.gaelrenoux.tranzactio.ConnectionSource
import io.github.gaelrenoux.tranzactio.doobie.{Database, tzio}
import zio.{ZIO, ZIOAppDefault, ZLayer, _}

case class UserReviewInput(userName: String, location: String, rate: Int, content: String, password: String)

case class ReviewRow(id: Int, userName: String, location: String, rate: Int, content: String)

case class ReviewRowWithPassword(id: Int, password: String)

object DBSampleApp extends ZIOAppDefault {
  def updateReviewById(id: Int, content: String, rate: Int) = {
    for {
      _ <- ZIO.unit
      database <- ZIO.service[Database]
      rows <- database
        .transactionOrWiden(for {
          res <- tzio {
            sql"""|UPDATE review
                  |SET content = ${content},
                  |rate = ${rate}
                  |where id = ${id}
                 """.stripMargin
              .update
              .run
          }
        } yield res)

      _ <- zio.Console.printLine(rows)
    } yield ()
  }

  def readReviews(username: String) = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|SELECT id, user_name, location, rate, content
                |FROM review
                |WHERE user_name = ${username};
             """.stripMargin

            .query[ReviewRow]
            .to[List]

        }
      } yield res)
  } yield (rows)

  def readReviewById(id: Int, password: String) = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|SELECT id, password
                |FROM review
                |WHERE id = ${id} and password = ${password};""".stripMargin
            .query[ReviewRowWithPassword]
            .option
        }
      } yield res)
  } yield (rows)

  def greetingServiceSelectInput = {
    for {
      _ <- Console.printLine("안녕하세요!😀 맛집 리뷰 서비스입니다!")
      _ <- Console.printLine("1: 리뷰 입력️")
      _ <- Console.printLine("2: 리뷰 조회")
      _ <- Console.printLine("3: 리뷰 삭제")
      _ <- Console.printLine("4: 리뷰 수정")

      userSelect <- Console.readLine("기능을 선택해주세요. : ")
    }
    yield (userSelect)
  }

  def createReviewInput = {
    for {
      _ <- Console.printLine("안녕하세요!😀 맛집 리뷰 서비스입니다!")
      _ <- Console.printLine("아래의 정보들을 순서대로 입력해주세요! ⭐️")
      username <- Console.readLine("고객님의 이름을 알려주세요 : ").map(_.trim)
      loc <- Console.readLine("맛집이 어디인지 주소나 간단한 위치 정보를 알려주세요. : ")
      rate <- Console.readLine("맛집의 별점을 입력해주세요! (1 ~ 5) : ").map(_.toInt)
      _ <- ZIO.when(rate > 5 || rate < 1) {
        ZIO.fail(new Exception("별점은 1점에서 5점까지 입력 가능합니다. 프로그램을 다시 시작해주세요!"))
      }
      content <- Console.readLine("특별이 맛있었거나 좋았던 점을 알려주세요! : ").map(_.trim)
      pw <- Console.readLine("해당 글을 수정/삭제하기 위해서는 추후에 비밀번호가 필요합니다. 비밀번호를 입력해주세요. : ").map(_.trim)

      result = UserReviewInput(username, loc, rate, content, pw)
      _ <- Console.printLine("입력이 완료되었습니다!")
    }
    yield (result)
  }

  def insertReview(userName: String, password: String, location: String, content: String, rate: Int) = for {
    _ <- ZIO.unit
    database <- ZIO.service[Database]
    rows <- database
      .transactionOrWiden(for {
        res <- tzio {
          sql"""|INSERT INTO review
                |(user_name
                |,password,
                | location,
                | content,
                |  rate)
                |VALUES
                |(${userName},
                | ${password},
                |  ${content},
                |  ${location},
                |  ${rate})
             """.stripMargin
            .update
            .run
        }
      } yield res)

    _ <- zio.Console.printLine(rows)
  } yield ()

  def deleteReview(id: Int) = {
    for {
      _ <- ZIO.unit
      database <- ZIO.service[Database]
      rows <- database
        .transactionOrWiden(for {
          res <- tzio {
            sql"""|DELETE
                  |FROM review
                  |WHERE id = ${id}
                 """.stripMargin
              .update
              .run
          }
        } yield res)

      _ <- zio.Console.printLine(rows)
    } yield ()
  }

  override def run = {
    for {
      userSelect <- greetingServiceSelectInput

      _ <- ZIO.when(userSelect == "1") {
        for {
          _ <- Console.printLine("리뷰 입력을 시작합니다.")
          userInput <- createReviewInput
          _ <- insertReview(userInput.userName, userInput.password, userInput.password, userInput.content, userInput.rate).provide(
            conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
          )
        } yield ()
      }

      _ <- ZIO.when(userSelect == "2") {
        for {
          _ <- Console.printLine("리뷰 조회를 시작합니다.")
          username <- Console.readLine("사용자 이름을 입력해주세요 : ")
          reviewList <- readReviews(username).provide(
            conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
          )

          _ <- ZIO.foreachDiscard(reviewList) {
            review => {
              for {
                _ <- Console.printLine("==================================")
                _ <- Console.printLine(s"id : ${review.id}")
                _ <- Console.printLine(s"작성자 : ${review.userName}")
                _ <- Console.printLine(s"평점 : ${review.rate}")
                _ <- Console.printLine(s"내용 : ${review.content}")
                _ <- Console.printLine(s"위치 : ${review.location}")
              } yield ()
            }
          }
        } yield ()
      }

      _ <- ZIO.when(userSelect == "3") {
        for {
          _ <- Console.printLine("리뷰를 삭제합니다.")
          reviewId <- Console.readLine("삭제할 리뷰의 ID를 입력해주세요 : ").map(_.toInt)
          password <- Console.readLine("삭제할 리뷰의 비밀번호를 입력해주세요 : ")
          review <- readReviewById(reviewId, password).provide(
            conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
          )
          _ <- review match {
            case Some(n) =>
              deleteReview(reviewId).provide(
                conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
              ) *> Console.printLine(s"리뷰(id : ${n.id})를 삭제했습니다.")
            case None =>
              Console.printLine("해당 리뷰를 찾을 수 없습니다. id 또는 비밀번호를 확인해주세요")
          }
        } yield ()
      }

      _ <- ZIO.when(userSelect == "4") {
        for {
          _ <- Console.printLine("리뷰를 수정합니다.")
          reviewId <- Console.readLine("수정할 리뷰의 ID를 입력해주세요 : ").map(_.toInt)
          password <- Console.readLine("삭제할 리뷰의 비밀번호를 입력해주세요 : ")

          review <- readReviewById(reviewId, password).provide(
            conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
          )

          _ <- review match {
            case Some(n) =>
              Console.printLine("해당 리뷰가 있음을 확인했습니다.") *>
                Console.readLine("수정할 리뷰의 본문을 입력해주세요 : ").flatMap(content =>
                  Console.readLine("수정할 리뷰의 평점을 을 입력해주세요 : ").map(_.toInt).flatMap(rate => {
                    if (rate > 5 || rate < 1) {
                      throw new Exception("별점은 1점에서 5점까지 입력 가능합니다. 프로그램을 다시 시작해주세요!")
                    }
                    updateReviewById(reviewId, content, rate).provide(
                      conn >>> ConnectionSource.fromConnection >>> Database.fromConnectionSource
                    ) *> Console.printLine(s"리뷰(id : ${n.id})를 수정했습니다.")
                  })
                )
            case None =>
              Console.printLine("해당 리뷰를 찾을 수 없습니다. id 또는 비밀번호를 확인해주세요")
          }
        } yield ()
      }
    } yield ()
  }

  val postgres = locally {
    val path = "localhost:5432"
    val name = "temp"
    val user = "hwimin"
    val password = "1234"
    s"jdbc:postgresql://$path/$name?user=$user&password=$password"
  }

  private val conn = ZLayer(
    ZIO.attempt(
      java.sql.DriverManager.getConnection(
        postgres
      )
    )
  )
}
