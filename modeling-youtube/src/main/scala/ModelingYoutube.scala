import zio._

object ModelingYoutube extends ZIOAppDefault {
  override def run = for {
    _ <- Console.printLine("Start")

    videoUris = List("Video1URI", "Video2URI", "Video3URI", "Video4URI")
    videoTitles = List(
      "Video1Title",
      "Video2Title",
      "Video3Title",
      "Video4Title"
    )
    videoViewCounts = List(178, 2049, 357, 657843)
    videoDurations = List(12345, 23451, 34512, 45123)

    videoData = videoUris
      .lazyZip(videoTitles)
      .lazyZip(videoViewCounts)
      .lazyZip(videoDurations)
      .toList
      .map(data => VideoData(data._1, data._2, data._3, data._4))

    videos = videoData.map(data => {
      val rand = scala.util.Random
      val isShort = rand.nextBoolean()

      if (isShort) ShortsContainer(data)
      else NormalContainer(data)
    })

    youtube = Youtube(videos)

    _ <- Console.printLine(youtube)
    _ <- Console.printLine("-----")

    sumCount = youtube.containers
      .map(container =>
        container match {
          case NormalContainer(data) => data.viewCount
          case ShortsContainer(data) => data.viewCount
        }
      )
      .sum

    sumDuration = youtube.containers
      .map(container =>
        container match {
          case NormalContainer(data) => data.duration
          case ShortsContainer(data) => data.duration
        }
      )
      .sum

    average <- ZIO
      .attempt(
        (
          sumCount / youtube.containers.length,
          sumDuration / youtube.containers.length
        )
      )
      .catchAll(err => ZIO.fail(err))

    _ <- Console.printLine(
      s"Average view count of videos: ${average._1} views!"
    )
    _ <- Console.printLine(
      s"Average duration of videos: ${average._2} seconds!"
    )

  } yield ()
}

case class VideoData(
    uri: String,
    title: String,
    viewCount: Long,
    duration: Long
)

sealed trait Container
case class NormalContainer(data: VideoData) extends Container
case class ShortsContainer(data: VideoData) extends Container

case class Youtube(containers: List[Container])
