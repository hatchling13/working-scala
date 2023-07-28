import zio._

object Main extends ZIOAppDefault {
  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")

    shorts1 = ShortContainer(Video("https://...", "title1", 60, 200))
    shorts2 = ShortContainer(Video("https://...", "title2", 30, 100))
    shorts3 = ShortContainer(Video("https://...", "title3", 100, 100))
    nomal1 = NomalContainer(Video("https://...", "title4", 2400, 30))
    youtube = Youtube(List(shorts1, shorts2, shorts3, nomal1))

    shortsViewSum = youtube.containers.map(
      container => container match {
        case ShortContainer(video) => video.viewCount
        case NomalContainer(video) => 0
      }
    ).sum

    shortsCount = youtube.containers.map(
      container => container match {
        case ShortContainer(video) => true
        case NomalContainer(video) => false
      }
    ).count(c => true)

    avgCount = shortsViewSum / shortsCount

    _ <- zio.Console.printLine(youtube)
    _ <- zio.Console.printLine(s"youtube 총 조회수는 $shortsViewSum 입니다.")
    _ <- zio.Console.printLine(s"youtube 평균 조회수는 $avgCount 입니다.")

  } yield ()
}

sealed  trait Container
case class Video(uri: String, title: String, sec: Long, viewCount: Long)
case class NomalContainer(video: Video) extends Container
case class ShortContainer(video: Video) extends Container
case class Youtube(containers: List[Container])