import cats.effect.std.Queue
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import com.typesafe.scalalogging.LazyLogging
import file.FileReader
import fs2.Stream
import http.WebsocketHttpServer
import kafka.SumProducer
import model.QueueMessage

object Main extends IOApp with LazyLogging {

  val config = 5

  override def run(args: List[String]): IO[ExitCode] = {
    logger.info("Starting CsvAdder application")
    for {
      reader <- FileReader[IO](config)
      fileStreams <- reader.fileStream("src/main/resources/numbers.csv")
      queue <- Queue.bounded[IO, QueueMessage](100)
      _ <- fileStreams.evalTap { case (index, value) => queue.offer(QueueMessage(index, value)) }.pure[IO]
      sumProducer <- SumProducer[IO](queue)
      websocketServer <- WebsocketHttpServer[IO](fileStreams)
      _ = logger.info("Websocket started")
      exitCode <- Stream(fileStreams, websocketServer.stream, sumProducer.stream)
        .parJoinUnbounded
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }
}
