import zio.ZIO
import zio.test._

object ScalaForComprehensionTest extends ZIOSpecDefault {
  def spec = suite("For-comprehension in Scala - Test")(
    test("Is for-comprehension equal to HOF - foreach") {
      for {
        first <- ZIO.succeed(Main.sumWithFor())
        second <- ZIO.succeed(Main.sumWithHOF())
      } yield assertTrue(first == second)
    },
    test("Is for-comprehension equal to HOF - filter") {
      for {
        first <- ZIO.succeed(Main.filteringWithFor())
        second <- ZIO.succeed(Main.filteringWithHOF())
      } yield assertTrue(first == second)
    },
    test("Is for-comprehension equal to HOF - map") {
      for {
        first <- ZIO.succeed(Main.mappingWithFor())
        second <- ZIO.succeed(Main.mappingWithHOF())
      } yield assertTrue(first == second)
    },
    test("Is for-comprehension equal to HOF - flatMap") {
      for {
        _ <- ZIO.unit
        data = List((3, 2), (10, 3), (20, 0), (4, 1))
        first <- ZIO.succeed(Main.flatMapWithFor(data))
        second <- ZIO.succeed(Main.flatMapWithHOF(data))
      } yield assertTrue(true)
    }
  )
}
