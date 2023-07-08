import zio._
import zio.http.{ZClient, _}

object Main extends ZIOAppDefault {

  val app =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "text" =>
        for {
          _ <- zio.Console.printLine("/text endpoint!")
          res <- ZIO.succeed(Response.text("Hello World!"))
        } yield res
      case Method.GET -> Root / "apple" =>
        for {
          _ <- zio.Console.printLine("/apple endpoint!")
          res <- ZIO.succeed(Response.text("APPLE!"))
        } yield res
      case Method.GET -> Root =>
        for {
          _ <- zio.Console.printLine("root endpoint!")
          url = URL.decode("http://localhost:13333/apple").toOption.get
          res <- ZClient.request(Request.get(url))
        } yield res
    }

  override val run =
    Server
      .serve(app.withDefaultErrorResponse)
      .provideLayer(Server.defaultWithPort(13333) ++ Client.default)
}
