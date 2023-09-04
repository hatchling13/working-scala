import zio.test.{Spec, TestClock, TestEnvironment, TestRandom, ZIOSpecDefault, assertTrue}
import zio.{Random, Scope, ZIO, durationInt}

object TestClockTest extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("TestClockTest")(
    test("TestClockTest") {
      for {
        fiber <- LazyTurtle.run().fork
        _ <- TestClock.adjust(10.seconds)
        turtle <- fiber.join
      } yield assertTrue(turtle.position == 10)
    }
  )
}
