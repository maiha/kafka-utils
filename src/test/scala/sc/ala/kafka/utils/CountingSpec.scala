package sc.ala.kafka.utils

import org.scalatest._

/**
 * under construction
 */
class CountingSpec extends TestHelper {
  describe("count()") {
    it("can be compiled") {
      onFirstTopic{ t => 
        utils.count(t)
        utils.counts(t)
      }
    }
  }
}
