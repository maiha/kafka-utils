package sc.ala.kafka.utils

import org.scalatest._

/**
 * under construction
 */
class OneHopSpec extends TestHelper {
  describe("topics()") {
    it("return Seq[String]") {
      debug(s"topics(): ${utils.topics()}")
      assert(utils.topics().isInstanceOf[Seq[String]])
    }
  }

  describe("brokers()") {
    it("return Seq(Broker)") {
      assert(utils.brokers().map(_.port) === Seq(9092))
    }
  }
}
