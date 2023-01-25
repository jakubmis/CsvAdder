package kafka

import cats.effect.kernel.Async
import cats.effect.std.Queue
import cats.implicits.catsSyntaxApplicativeId
import fs2.kafka.{KafkaProducer, ProducerResult, ProducerSettings}
import model.QueueMessage

class SumProducer[F[_] : Async](queue: Queue[F, QueueMessage]) {

  private val producerSettings: ProducerSettings[F, Int, Int] = ProducerSettings[F, Int, Int]
    .withBootstrapServers("localhost:29092")

  val stream: fs2.Stream[F, F[ProducerResult[Unit, Int, Int]]] = KafkaProducer
    .stream(producerSettings)
    .flatMap(producer =>
      fs2.Stream
        .fromQueueUnterminated(queue)
        .evalMap(update => producer.produceOne("summed_file", update.index, update.value, ())),
    )
}

object SumProducer {
  def apply[F[_] : Async](queue: Queue[F, QueueMessage]): F[SumProducer[F]] = {
    new SumProducer(queue).pure[F]
  }
}
