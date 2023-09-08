import zio._
import zio.test.{test, _}
import zio.test.Assertion._
import java.io.IOException
import RandomVendingMachine._

// https://zio.dev/reference/test/services/random/#feeding-predefined-random-values
object RandomMocking extends ZIOSpecDefault {
  val spec
  = suite("random mock")(
      test("case if item is soda") {
        for {
          _ <- TestRandom.feedInts(0)
          result <- RandomVendingMachine.warnSoda
        } yield {
          assertTrue(result == "[삐빅!!! 탄산음료에요...]")
        }
      }
    )
    test("not soda") {
        for {
          _ <- TestRandom.feedInts(1)
          result <- RandomVendingMachine.warnSoda
        } yield {
          assertTrue(result == "[좋은 선택입니다]")
        }
      }
}

