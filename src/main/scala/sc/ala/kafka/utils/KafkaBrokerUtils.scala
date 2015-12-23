package sc.ala.kafka.utils

import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.protocol.SecurityProtocol
import org.apache.kafka.common.security.JaasUtils
import org.apache.kafka.common.serialization.ByteArrayDeserializer
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

  def headOffsets(topic: String, partitions: Seq[Int]): Map[Int, Long] = {
    val tps = partitions.map(new TopicPartition(topic, _))
    consumer.assign(tps)
    consumer.seekToBeginning()
    tps.map(tp => tp.partition -> consumer.position(tp)).toMap
  }

  def lastOffsets(topic: String, partitions: Seq[Int]): Map[Int, Long] = {
    val tps = partitions.map(new TopicPartition(topic, _))
    consumer.assign(tps)
    consumer.seekToEnd()
    tps.map(tp => tp.partition -> consumer.position(tp)).toMap
  }
}

case class KafkaBrokerUtilsContext(consumer: KafkaConsumer[Array[Byte], Array[Byte]]) extends KafkaBrokerUtils {
  def apply[A](action: KafkaBrokerUtils => A): A = {
    try {
      action(this)
    } finally {
      consumer.close()
    }
  }
}

object KafkaBrokerUtils {
  def newConsumer(brokerList: String) = {
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer])
    new KafkaConsumer[Array[Byte], Array[Byte]](props)
  }

  def apply(brokerList: String = "localhost:9092"): KafkaBrokerUtils = KafkaBrokerUtilsContext(newConsumer(brokerList))
}
