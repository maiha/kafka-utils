package sc.ala.kafka.utils

import org.scalatest._

class OffsetSpec extends TestHelper {
  describe("Offset") {
    it("offset") {
      utils.topics.take(10).sorted.foreach{ t =>
        val v = utils.offset(t)
        println(s"topic($t).offset = $v")
      }
    }

    it("offsets") {
      utils.topics.take(10).sorted.foreach{ t =>
        val v = utils.offsets(t)
        println(s"topic($t).offsets = $v")
      }
    }
  }
}
