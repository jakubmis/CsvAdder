package http

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.{CodecFormat, PublicEndpoint, endpoint, query, webSocketBody}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

class WebsocketHttpServer[F[_] : Async](fileStreams: Stream[F, (Int, Int)]) {

  val wsEndpoint: PublicEndpoint[Int, Unit, Pipe[F, String, String], Fs2Streams[F] with WebSockets] =
    endpoint
      .get
      .in("")
      .in(query[Int]("number"))
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[F]))

  def pipe(number: Int): Pipe[F, String, String] = {
    _ =>
        fileStreams
          .filter(_._1 == number)
          .map { case (_, y) => y }
          .map(_.toString)
  }

  val wsRoutes: WebSocketBuilder2[F] => HttpRoutes[F] =
    Http4sServerInterpreter[F]().toWebSocketRoutes(wsEndpoint.serverLogicSuccess[F](number => pipe(number).pure[F]))

  val stream: Stream[F, ExitCode] =
    BlazeServerBuilder.apply[F]
      .withConnectorPoolSize(10)
      .withoutBanner
      .withResponseHeaderTimeout(Duration(10, TimeUnit.SECONDS))
      .withIdleTimeout(Duration(10, TimeUnit.SECONDS))
      .bindHttp(8080, "localhost")
      .withHttpWebSocketApp(wsb => Router("/ws" -> wsRoutes(wsb)).orNotFound)
      .serve
}

object WebsocketHttpServer {
  def apply[F[_] : Async](fileStreams: Stream[F, (Int, Int)]): F[WebsocketHttpServer[F]] = {
    new WebsocketHttpServer(fileStreams).pure[F]
  }
}
