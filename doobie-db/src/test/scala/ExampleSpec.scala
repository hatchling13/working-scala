import zio.test._

object ExampleSpec extends ZIOSpecDefault {
  def spec =
    suite("ConsoleTest")(
      test("insert today's mood") {
        assertTrue(true)
      }
        test ("ahfsgd") {
          assertTrue(true)
        }
        test ("test1") {
          assertTrue(true)
        }
    ).provideShared()
}
