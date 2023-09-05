import zio.test.{Spec, TestClock, TestConsole, TestEnvironment, TestRandom, ZIOSpecDefault, assertTrue}
import zio.{Random, Scope, ZIO}

object TestConsoleTest extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("TestConsoleTest")(
    test("TestConsoleTest") {
      for {
        _ <- ZIO.unit
        name = "김지오"
        age = 50
        _ <- TestConsole.feedLines(name, Integer.toString(age))
        user <- InputUserInfo.run()
      } yield {
        assertTrue(user.name == name) &&
          assertTrue(user.age == age)
      }
    }
  )

}
