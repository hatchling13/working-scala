import zio.{ZIO, ZIOAppDefault, ZLayer}
import java.io.IOException


object DITest2 extends ZIOAppDefault {
  trait 식당저장소 {
    def 모든식당이름가져오기(): ZIO[Any, Nothing, List[String]]
  }


  class 공덕식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("김밥집", "피자집", "샌드위치집"))
  }

  class 판교식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("카카오국밥", "네이버국밥", "엔씨국밥"))
  }

  object 판교식당저장소 {
    val layer = ZLayer.succeed(new 판교식당저장소)
  }


  // 판교식당저장소가 주입되었다.
  def useCaseUsingZLayer(): ZIO[식당저장소, IOException, Unit] = for {
    repo <- ZIO.service[식당저장소]
    _ <- ZIO.unit
    list <- repo.모든식당이름가져오기()
    _ <- zio.Console.printLine(list) // List(카카오국밥, 네이버국밥, 엔씨국밥)
  } yield ()

  
  override def run = for {
    _ <- ZIO.unit
    _ <- useCaseUsingZLayer().provideLayer(판교식당저장소.layer)
  } yield ()
}
