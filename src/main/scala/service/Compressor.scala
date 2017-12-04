package service

import akka.actor.{Actor, ActorLogging, Props}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

trait Compressor {

  /**
    * This method implements the Run-Length Encoding algorithm.
    *
    * @tparam A data type to encode
    * @return The encoded data in the form (count, A)
    */
  def compress[A]: List[A] => Seq[Repeat[A]] = runs => {

    @tailrec
    def loop(internRuns: List[A], acc: ListBuffer[Repeat[A]], element: A, occurences: Int): Seq[Repeat[A]] = {
      if(internRuns.isEmpty) {
        // creates a repeat for the last element
        acc += Repeat(occurences, element)
      } else {
        if(internRuns.head == element) loop(internRuns.tail, acc, element, occurences + 1)
        else {
          // creates a repeat when the element value changes and set the occurences to the default value 1 for the next loop
          acc += Repeat(occurences, element)
          loop(internRuns.tail, acc, internRuns.head, 1)
        }
      }
    }

    loop(runs, ListBuffer.empty[Repeat[A]], runs.head, 0).toList
  }

}

case class Repeat[A](count: Int, element: A)

/**
  * This actor is responsible for encoding the data and to hold the newest result in memory.
  */
class CompressorClient extends Actor with Compressor with ActorLogging {

  private var compressionCache: Seq[Repeat[Char]] = _

  override def receive: Receive = {

   case Compress(runs) =>
     compressionCache = compress.apply(runs)
     log.info("the compression is done for incoming runs")

    case GetIndex(index) => sender ! GetIndexResponse(elementAt(index))
  }

  private def elementAt(index: Int): Option[Char] = {
    @tailrec
    def loop(lIndex: Int, lCompression: Seq[Repeat[Char]]): Option[Char] = {
      if(index < lIndex) Some(lCompression.head.element)
      else {
        val ltail = lCompression.tail
        if(ltail.isEmpty) None
        else loop(lIndex + ltail.head.count, ltail)
      }
    }

    loop(compressionCache.head.count, compressionCache)
  }

}

object CompressorClient {

  def props(): Props = Props(new CompressorClient())
}