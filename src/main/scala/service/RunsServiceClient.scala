package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Framing, Sink}
import akka.util.ByteString
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class RunsServiceClient private(runsServiceUrl: String) {

  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  def fetchRuns()(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[List[Char]] = {
    val futureResponseEntity = for (response <- Http().singleRequest(HttpRequest(uri = runsServiceUrl))) yield response.entity

    futureResponseEntity.flatMap { entity =>
      // the flow for handling the entity data as frame with the new line delimiter
      val flow = Flow[ByteString]
        .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
        .fold(ByteString.empty)((acc, byteString) => {
          acc ++ byteString
        })

      val sinkResponse: Sink[ByteString, Future[ByteString]] = Sink.head[ByteString].contramap(byteString => {
        LOGGER.info(s"runs service response size - ${byteString.size}")
        byteString
      })

      entity.dataBytes.via(flow).runWith(sinkResponse).map(_.utf8String.toList)
    }
  }

}

object RunsServiceClient {

  def apply(runsServiceUrl: String) = new RunsServiceClient(runsServiceUrl)

}
