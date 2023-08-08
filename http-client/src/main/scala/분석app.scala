// import zio._
// import zio.json._

// object 분석app extends ZIOAppDefault {
//    trait 식당저장소 {
//     def 모든식당정보가져오기(): ZIO[Any, Nothing, List[String]]
//     def 식당정보가져오기(name: String) : ZIO[Any, Nothing, List[String]]
//   }

//   class 공덕식당저장소 extends 식당저장소 {
//     def 모든식당정보가져오기() = ZIO.succeed(List("세끼김밥집", "마녀김밥집","오토김밥"))
//     def 식당정보가져오기(name: String) = ZIO.succeed(List("매운김밥"))
//   }
//     class 판교식당저장소 extends 식당저장소 {
//     def 모든식당정보가져오기() = ZIO.succeed(List("카카오구내식당", "NC소프트구내식당","SK구내식당"))
//     def 식당정보가져오기(name: String) = ZIO.succeed(List("급식"))
//   }

// object 식당저장소{

//     val useCaseUsingZlayer(name: String) = for {
//     b  <- ZIO.service[식당저장소]
//     list <- repo.모든식당이름가져오기()

//     _ <- zio.Console.printLine(list)

//     a <- zio.Console.readLine("입력")

//     // _ <- 예약정보등록하기(a)

//   } yield ()

//   val useCase(repo: 식당저장소) = for {
//     list  <- repo.모든식당정보가져오기()

//     _ <- zio.Console.printLine(list)

//     a <- zio.Console.readLine("입력")

//     // _ <- 예약정보등록하기(a)

//   } yield ()

// }

//   override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] =
//     for {
//       _ <- ZIO.unit
//       // _ <- useCase(new 공덕식당저장소)
//       _ <- useCaseUsingZlayer.provideLayer(공덕식당저장소.layer)
//     } yield ()

// }
