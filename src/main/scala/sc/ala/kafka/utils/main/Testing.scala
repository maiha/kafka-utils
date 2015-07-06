package sc.ala.kafka.utils.main

import sc.ala.kafka.utils._
import pl.project13.scala.rainbow._

/**
 * testing
 */
private[utils] object Testing {
  val zkConnect = "localhost:2181"

  def main(args: Array[String]) {
    zk()
    brokers()
    topics()
    partitions()
    leaders()
    metadata()
    counts()
  }

  private def utils = KafkaUtils.run(zkConnect)

  private def zk() = {
    println(s"zkConnect: $zkConnect".yellow)
  }

  private def brokers() = {
    println("brokers:".green)
    println(s"  -> ${utils(_.brokers)}")
  }

  private def topics() = {
    println("topics:".green)
    println(s"  -> ${utils(_.topics)}")
  }

  private def partitions() = {
    println(s"partitions()".green)
    utils{ u =>
      u.topics.foreach{ t =>
        val ps = u.partitions(t)
        println(s"  -> $t: ${ps}".green)
        ps.foreach{ p =>
          val l = u.leader(t, p)
          println(s"     -> $p: $l")
        }
      }
    }
  }

  private def leaders() = {
    println("leaders()".green)
    utils{ u =>
      u.topics.foreach{ t =>
        println(s"  -> $t:".green)
        println(s"     broker.id=${u.leaders(t)}")
      }
    }
  }

  private def metadata() = {
    println("metadata()".green)
    utils{ u =>
      u.topics.foreach{ t =>
        println(s"  -> $t:".green)
        println(s"     ${u.metadata(t)}")
      }
    }
  }

  private def counts() = {
    println("counts()".green)
    utils{ u =>
      u.topics.foreach{ t =>
        val sum = u.count(t)
        val map = u.counts(t).filter(_._2 > 0)
        println(s"  -> $t:".green)
        println(s"     ${sum} ($map)")
      }
    }
  }
}
