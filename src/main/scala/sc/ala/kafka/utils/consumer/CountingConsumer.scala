package sc.ala.kafka.utils.consumer

import kafka.api._
import kafka.utils._
import kafka.consumer._
import kafka.consumer.ConsumerConfig.{SocketTimeout, SocketBufferSize}
import kafka.client.ClientUtils
import kafka.cluster.Broker
import kafka.common.TopicAndPartition
import scala.collection.JavaConversions._
import org.apache.kafka.common.utils.Utils

import sc.ala.rubyist.Using._
import pl.project13.scala.rainbow._

class CountingConsumer(host: String, port: Int, topic: String, partition: Int) extends Logging {
  private def clientId = "api-count"
  private def newSimpleConsumer = new SimpleConsumer(host, port, SocketTimeout, SocketBufferSize, clientId)

  def count(): Long = {
    using(newSimpleConsumer)(countingImpl)
  }

  // derived from "kafka.tools.SimpleConsumerShell"
  private def countingImpl(consumer: SimpleConsumer): Long = {
    var numMessagesConsumed = 0

    val maxMessages = 100000000
    val skipMessageOnError = true
    val replicaId = -1

    val printDebug = false
    
    def buildFetchRequest(offset: Long, fetchSize: Int) = {
      val requestInfos = Map(
        TopicAndPartition(topic, partition) -> PartitionFetchInfo(offset, fetchSize)
      )

      new FetchRequest(
        clientId    = clientId,
        replicaId   = Request.DebuggingConsumerId,  // Request.OrdinaryConsumerId
        maxWait     = 100,
        minBytes    = FetchRequest.DefaultMinBytes,
        requestInfo = requestInfos
      )
    }

    val thread = Utils.newThread(s"$clientId-thread", new Runnable() {
      def run() {
        var offset = resetOffset(topic, partition)
        try {
          while(numMessagesConsumed < maxMessages) {
            val fetchRequest  = buildFetchRequest(offset, 1000000)
            val fetchResponse = consumer.fetch(fetchRequest)
            val messageSet    = fetchResponse.messageSet(topic, partition)
            if (messageSet.validBytes <= 0) {
              if (printDebug) println(s"messageSet not found. exiting".yellow)
              return
            } else {
              if (printDebug) println(s"fetchResponse=$fetchResponse, $messageSet".green)
            }
            if (printDebug) println("multi fetched " + messageSet.sizeInBytes + " bytes from offset " + offset)
            for(messageAndOffset <- messageSet if(numMessagesConsumed < maxMessages)) {
              try {
                offset = messageAndOffset.nextOffset
                if(printDebug) println("next offset = " + offset)
                val message = messageAndOffset.message
                val key = if(message.hasKey) Utils.readBytes(message.key) else null
                numMessagesConsumed += 1
              } catch {
                case e: Throwable =>
                  if (skipMessageOnError)
                    error("Error processing message, skipping this message: ", e)
                  else
                    throw e
              }
              if(System.out.checkError()) {
                // This means no one is listening to our output stream any more, time to shutdown
                System.err.println("Unable to write to standard out, closing consumer.")
                consumer.close()
                throw new RuntimeException("")
              }
            }
          }
        } catch {
          case e: Throwable =>
            error("Error consuming topic, partition, replica (%s, %d, %d) with offset [%d]".format(topic, partition, replicaId, offset), e)
        }finally {
          info("Consumed " + numMessagesConsumed + " messages")
        }
      }
    }, false)

    thread.start()
    thread.join()
    System.out.flush()
    consumer.close()

    numMessagesConsumed
  }

  private def resetOffset(topic: String, partition: Int): Long =
    using(newSimpleConsumer) { _.earliestOrLatestOffset(TopicAndPartition(topic, partition), -2, Request.DebuggingConsumerId) }
}
