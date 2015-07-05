package sc.ala.kafka.utils

import org.scalatest._

/**
 * under construction
 */
class KafkaUtilsRestSpec extends TestHelper {
  val zkConnect: String = "localhost:2181"
  def utils: KafkaUtils = KafkaUtils(zkConnect)

  describe("topics()") {
    it("return Seq[String]") {
      debug(s"topics(): ${utils.topics()}")
      assert(utils.topics().isInstanceOf[Seq[String]])
    }
  }

  describe("leader(topic, partition)") {
    it("return Some(1)") {
      onFirstTopic{ t => 
        assert(utils.leader(t, 0) === Some(1))
      }
    }
  }

  describe("partitions(topicName)") {
    it("return Seq(0)") {
      onFirstTopic{ t => 
        assert(utils.partitions(t) === Seq(0))
      }
    }
  }

  describe("brokers()") {
    it("return Seq(Broker)") {
      onFirstTopic{ t => 
        assert(utils.brokers().map(_.port) === Seq(9092))
      }
    }
  }

  private def onFirstTopic[A](action: String => A): Unit = {
    utils.topics().headOption match {
      case Some(topic: String) => action(topic)
      case None => pending
    }
  }

  private def debug(msg: String) = Console.err.println(msg)

}
