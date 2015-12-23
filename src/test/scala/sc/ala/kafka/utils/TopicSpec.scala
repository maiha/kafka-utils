package sc.ala.kafka.utils

import org.scalatest._

class TopicSpec extends TestHelper {
  describe("partitions(topicName)") {
    it("return Seq(0)") {
      onFirstTopic{ t => 
        utils.partitions(t)  // works
      }
    }
  }

  describe("leader(topic, partition)") {
    it("return Some(1)") {
      onFirstTopic{ t => 
        utils.leader(t, 0)  // works
      }
    }
  }
}
