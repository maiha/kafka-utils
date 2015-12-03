package sc.ala.kafka.utils

import kafka.api.{TopicMetadataRequest, TopicMetadataResponse}
import kafka.cluster.Broker
import java.util.Properties
import scala.util._

private[utils] trait Api {
  // 1 hop api
  def brokers(): Seq[Broker]
  def topics(): Seq[String]

  // topics
  def leader(topic: String, partition: Int): Option[Int]
  def leaders(topic: String): Map[Int, Option[Int]]
  def leaderBrokers(topic: String): Map[Int, Broker]
  def partitions(topic: String): Seq[Int]
  def count(topic: String): Long
  def create(topic: String, partitions: Int, replicationFactor: Int, topicConfig: Properties = new Properties): Unit
  def delete(topic: String): Unit

  // testing (api is not fixed yet)
  def metadata(topic: String): TopicMetadataResponse
  def metadatas(topic: String, partition: Int): Try[TopicMetadataResponse]

  // TODO
  def count(topic: String, partition: Int): Long
  def counts(topic: String): Map[Int, Long]
}
