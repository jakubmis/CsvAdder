package file

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

import scala.language.postfixOps

class FileReaderSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "FileReader " - {
    "read file and sum corresponding groups" in {
      FileReader[IO](5)
        .flatMap(_.fileStream("src/test/resources/numbers.csv"))
        .flatMap(_.compile.toVector).asserting {
        vector => vector shouldBe Vector((0, 15), (1, 7), (2, 9), (3, 11), (4, 13))
      }
    }
  }

}
