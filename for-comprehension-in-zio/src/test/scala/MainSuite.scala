import zio.ZIO
import zio.test._

object MainSuite extends ZIOSpecDefault {
  def spec = suite("ForComprehensionTest")(
    test("Is for-comprehension equal to HOF - flatMap") {
      for {
        _ <- Main.effect1
        _ <- Main.effect2
        output <- TestConsole.output
      } yield assertTrue(output.head == output.last)
    }
  )
}
