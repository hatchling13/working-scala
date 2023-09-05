import zio.{ZIO, ZIOAppDefault}


object DITest1 extends ZIOAppDefault {
  trait 식당저장소 {
    def 모든식당이름가져오기(): ZIO[Any, Nothing, List[String]]
  }


  class 공덕식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("김밥집", "피자집", "샌드위치집"))
  }

  class 판교식당저장소 extends 식당저장소 {
    def 모든식당이름가져오기() = ZIO.succeed(List("카카오국밥", "네이버국밥", "엔씨국밥"))
  }



  // 메서드에 주입된 의존성에 따라 다른 결과를 가져온다.
  def useCase(repo: 식당저장소) = for {
    _ <- ZIO.unit
    list <- repo.모든식당이름가져오기()
    _ <- zio.Console.printLine(list) // List(김밥집, 피자집, 샌드위치집)
  } yield ()



  override def run = for {
    _ <- ZIO.unit
    _ <- useCase(new 공덕식당저장소) // 의존성 넣기
  } yield ()
}


