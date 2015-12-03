package sc.ala.kafka.utils

import org.apache.kafka.common.protocol.SecurityProtocol
import org.apache.kafka.common.security.JaasUtils
import org.apache.zookeeper.ZooKeeper
import org.I0Itec.zkclient.ZkClient
import java.util.Properties
import kafka.admin.AdminUtils
import kafka.cluster.Broker
import kafka.consumer._
import kafka.api.{TopicMetadataRequest, TopicMetadataResponse}
import kafka.coordinator.GroupCoordinator.GroupMetadataTopicName
import kafka.utils._
import sc.ala.rubyist.Using._
import sc.ala.kafka.utils.consumer._

import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import collection.JavaConversions._

import pl.project13.scala.rainbow._

abstract class KafkaUtils extends Api {
  def zkClient: ZkClient

  def isSecure: Boolean = JaasUtils.isZkSecurityEnabled()

  def zkUtils: ZkUtils = ZkUtils(zkClient, isZkSecurityEnabled = isSecure)

  def brokers: Seq[Broker] = zkUtils.getAllBrokersInCluster()

  def topics: Seq[String] = zkUtils.getAllTopics().diff(reservedTopicNames)

  def leader(topic: String, partition: Int): Option[Int] = 
    zkUtils.getLeaderForPartition(topic, partition)

  def leaders(topic: String): Map[Int, Option[Int]] = partitions(topic).map(p =>
    (p, leader(topic, p))
  ).toMap

  def leaderBrokers(topic: String): Map[Int, Broker] = {
    val resolve = brokers.groupBy(_.id).map{ case (k, list) => (k, list.head) }
    try {
      leaders(topic).filter(_._2.isDefined).mapValues(_.map(resolve).get)
    } catch {
      case err: java.util.NoSuchElementException =>
        println(s"ERROR: topic=$topic, leaders=${leaders(topic)}".red)
        throw err
    }
  }

  def partitions(topic: String): Seq[Int] =
    zkUtils.getPartitionAssignmentForTopics(Seq(topic))(topic).keys.toSeq

  private def brokerEndPointFrom(b: Broker) = b.getBrokerEndPoint(SecurityProtocol.PLAINTEXT)

  def metadata(topic: String): TopicMetadataResponse = {
    val ps = partitions(topic)
    val bs = brokers()

    bs.foreach { b =>
      val bep = brokerEndPointFrom(b)
      ps.foreach { p =>
        return metadatas(bep.host, bep.port, topic, p).get  // needs only one result
      }
    }
    throw new RuntimeException("metadata not found: no active brokers")
  }

  def metadatas(topic: String, partition: Int): Try[TopicMetadataResponse] = Try {
    brokers.foreach { b =>
      val bep = brokerEndPointFrom(b)
      metadatas(bep.host, bep.port, topic, partition) match {
        case Success(md) => return Success(md)
        case _ =>  // next
      }
    }
    throw new RuntimeException(s"metadata not found: ($topic, $partition)")
  }

  def count(topic: String, partition: Int): Long = {
    import scala.concurrent.ExecutionContext.Implicits.global
    leaderBrokers(topic).get(partition) match {
      case Some(b) =>
        val bep = brokerEndPointFrom(b)
        val f = Future{ new CountingConsumer(bep.host, bep.port, topic, partition).count() }
        Await.result(f, 60.seconds)
      case None =>
        throw new RuntimeException(s"leader not found: ($topic, $partition)")
    }
  }

  def counts(topic: String): Map[Int, Long] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val sources = leaderBrokers(topic)  // Map(0 -> id:1,host:ubuntu,port:9092)
    val fetches = sources.map { case (p, b) => Future {
      val bep = brokerEndPointFrom(b)
      (p, new CountingConsumer(bep.host, bep.port, topic, p).count())
    } }
    Await.result(Future.sequence(fetches), 60.seconds).toMap
  }

  def count(topic: String): Long = counts(topic).values.sum

  def create(
    topic: String,
    partitions: Int,
    replicationFactor: Int,
    topicConfig: Properties = new Properties
  ) = AdminUtils.createTopic(zkUtils, topic, partitions, replicationFactor, topicConfig)

  def delete(topic: String): Unit = {
    AdminUtils.deleteTopic(zkUtils, topic)
  }

  def close(): Unit = zkClient.close()

  private def reservedTopicNames: Seq[String] = Seq(GroupMetadataTopicName)

  private def metadatas(brokerHost: String, brokerPort: Int, topic: String, partition: Int): Try[TopicMetadataResponse] = {
    val consumer = new SimpleConsumer(brokerHost, brokerPort, 100000, 64 * 1024, "leaderLookup")
    val topics   = Seq(topic)
    val req      = TopicMetadataRequest(TopicMetadataRequest.CurrentVersion, 0, TopicMetadataRequest.DefaultClientId, topics)
    val res: TopicMetadataResponse = consumer.send(req)
    Success(res)
  }
}

class KafkaUtilsContext(factory: => ZkClient) extends KafkaUtils {
  def zkClient = factory
  def open() = new KafkaUtils { val zkClient = factory }

  def apply[A](action: KafkaUtils => A): A = {
    val client = factory
    try {
      val utils = new KafkaUtils { val zkClient = client }
      action(utils)
    } finally {
      client.close()
    }
  }
}

object KafkaUtils {
  def newZkClient(s: String) = ZkUtils.createZkClient(s, 30000, 30000)

  def apply(zkConnect: String = "localhost:2181"): KafkaUtils = new KafkaUtils { val zkClient = newZkClient(zkConnect) }

  def run(zkConnect: String = "localhost:2181") = new KafkaUtilsContext(newZkClient(zkConnect))
}
