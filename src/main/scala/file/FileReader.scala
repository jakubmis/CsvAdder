package file

import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import com.typesafe.scalalogging.LazyLogging
import fs2.io.file.{Files, Path}
import fs2.{Pull, Stream, _}

class FileReader[F[_] : Async](config: Int) extends LazyLogging {
  def fileStream(resource: String): F[Stream[F, (Int, Int)]] = {
    Files[F]
      .readUtf8Lines(Path(resource))
      .groupAdjacentBy(_.toInt % config)
      .map { case (index, chunk) => (index, chunk.foldLeft(0)(_ + _.toInt)) }
      .through(groupByKey)
      .map { case (index, list) => (index, list.sum) }
      .pure[F]
  }

  private def groupByKey[K, V]: Pipe[F, (K, V), (K, List[V])] = {
    def iterate(state: Map[K, List[V]]): Stream[F, (K, V)] => Pull[F, (K, List[V]), Unit] = _.pull.uncons1.flatMap {
      case Some(((key, num), tail)) =>
        iterate(state.updated(key, num :: state.getOrElse(key, Nil)))(tail)
      case None =>
        val chunk = Chunk.vector(state.toVector)
        Pull.output(chunk) >> Pull.done
    }

    iterate(Map.empty)(_).stream
  }
}

object FileReader {
  def apply[F[_] : Async](config: Int): F[FileReader[F]] = {
    new FileReader[F](config).pure[F]
  }
}
