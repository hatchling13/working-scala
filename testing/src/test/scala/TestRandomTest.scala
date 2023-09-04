import zio.test.{Spec, TestConsole, TestEnvironment, TestRandom, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}

import java.io.IOException
import scala.util.Random

object TestRandomTest extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] = suite("TestRandomTest")(
    test("1이면 도망 성공") {
      for {
        _ <- TestRandom.feedInts(1)
        _ <- ZombieGame.run()
        output <- TestConsole.output
      } yield assertTrue(output(0) == "도망 성공!\n")
    }
  )
}
