package file

import cats.effect.kernel.Async
import cats.implicits.catsSyntaxApplicativeId
import fs2.{Pull, Stream}
import fs2.io.file.{Files, Path}
import cats.data.{NonEmptyList => Nel}
import fs2._

class FileReader[F[_] : Async](config: Int) {
  def fileStream(resource: String): F[Stream[F, (Int, Int)]] = {
    Files[F]
      .readUtf8Lines(Path(resource))
      .groupAdjacentBy(_.toInt % config)
      .map { case (index, chunk) => (index, chunk.foldLeft(0)(_ + _.toInt)) }
      .through(groupByKey(100))
      .map { case (index, list) => (index, list.foldLeft(0)(_ + _)) }
      .pure[F]
  }

  def groupByKey[F[_], K, V](limit: Int): Pipe[F, (K, V), (K, Nel[V])] = {
    def go(state: Map[K, List[V]]): Stream[F, (K, V)] => Pull[F, (K, Nel[V]), Unit] = _.pull.uncons1.flatMap {
      case Some(((key, num), tail)) =>
        val prev = state.getOrElse(key, Nil)
        if (prev.size == limit - 1) {
          val group = Nel.ofInitLast(prev.reverse, num)
          Pull.output1(key -> group) >> go(state - key)(tail)
        } else {
          go(state.updated(key, num :: prev))(tail)
        }
      case None =>
        val chunk = Chunk.vector {
          state
            .toVector
            .collect { case (key, last :: revInit) =>
              val group = Nel.ofInitLast(revInit.reverse, last)
              key -> group
            }
        }
        Pull.output(chunk) >> Pull.done
    }

    go(Map.empty)(_).stream
  }
}

object FileReader {
  def apply[F[_] : Async](config: Int): F[FileReader[F]] = {
    new FileReader[F](config).pure[F]
  }
}
