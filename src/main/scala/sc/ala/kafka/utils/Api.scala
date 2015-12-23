package sc.ala.kafka.utils

import kafka.api.{TopicMetadataRequest, TopicMetadataResponse}
import kafka.cluster.Broker
import java.util.Properties
import scala.util._

private[utils] trait Api {
  // reference
  def broker: KafkaBrokerUtils

  // 1 hop api
  def brokers(): Seq[Broker]
  def topics(): Seq[String]
  def brokerConnectString: String

  // topics
  def leader(topic: String, partition: Int): Option[Int]
  def leaders(topic: String): Map[Int, Option[Int]]
  def leaderBrokers(topic: String): Map[Int, Broker]
  def partitions(topic: String): Seq[Int]
  def count(topic: String): Long
  def counts(topic: String): Map[Int, Long]
  def create(topic: String, partitions: Int, replicationFactor: Int, topicConfig: Properties = new Properties): Unit
  def delete(topic: String): Unit
  def offset(topic: String): Long
  def offsets(topic: String): Map[Int, Long]

  // testing (api is not fixed yet)
  def metadata(topic: String): TopicMetadataResponse
  def metadatas(topic: String, partition: Int): Try[TopicMetadataResponse]

  // TODO
}
