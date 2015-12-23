package sc.ala.kafka.utils

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
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

abstract class KafkaBrokerUtils {
  def consumer: KafkaConsumer[Array[Byte], Array[Byte]]

  def offsets(topic: String, partitions: Seq[Int]): Map[Int, Long] = {
    val tps = partitions.map(new TopicPartition(topic, _))
    consumer.assign(tps)
    consumer.seekToEnd()
    tps.map(tp => tp.partition -> consumer.position(tp)).toMap
  }
}

class KafkaBrokerUtilsContext(kafkaConsumer: KafkaConsumer[Array[Byte], Array[Byte]]) extends KafkaBrokerUtils {
//  def open() = new KafkaBrokerUtils { val zkClient = factory }
  val consumer = kafkaConsumer

  def apply[A](action: KafkaBrokerUtils => A): A = {
    try {
      action(this)
    } finally {
//      client.close()
    }
  }
}

object KafkaBrokerUtils {
  def newConsumer(brokerList: String) = {
    val config = new Properties
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[org.apache.kafka.common.serialization.ByteArrayDeserializer])
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[org.apache.kafka.common.serialization.ByteArrayDeserializer])
    new KafkaConsumer[Array[Byte], Array[Byte]](config)
  }

  def apply(brokerList: String = "localhost:9092"): KafkaBrokerUtils = new KafkaBrokerUtils { val consumer = newConsumer(brokerList) }

  def run(brokerList: String = "localhost:9092") = new KafkaBrokerUtilsContext(newConsumer(brokerList))
}
