import zio._
import Util._

object Main extends ZIOAppDefault {

  val prog = for {

    action <- Controller.getAction()

  } yield ()

  override def run = prog
    .provide(Postgres.DBLayer)
}
