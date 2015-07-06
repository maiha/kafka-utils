package sc.ala.kafka.utils

import org.scalatest._

private[utils] trait TestHelper extends FunSpec {
  protected val zkConnect: String = "localhost:2181"
  protected def utils: KafkaUtils = KafkaUtils(zkConnect)

  protected def onFirstTopic[A](action: String => A): Unit = {
    utils.topics().headOption match {
      case Some(topic: String) => action(topic)
      case None => pending
    }
  }

  protected def debug(msg: String) = Console.err.println(msg)
}
