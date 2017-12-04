package service

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
  * This object represents the compressor service and provide the rest api for accessing encoding data over the index.
  */
object CompressionService extends Directives with JsonSupport {

  private implicit val system = ActorSystem("CompressionService")
  private implicit val materializer = ActorMaterializer()
  private implicit val executor = system.dispatcher
  private implicit val requestTimeout = Timeout(2.seconds)

  private val logger = Logging(system, getClass)
  private val compressor = system.actorOf(CompressorClient.props())
  private val runsServiceClient = RunsServiceClient("http://localhost:8080")

  def start(): Unit = {
    startScheduler()

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

  private def route: Route =
    path(IntNumber) { index =>
      get {
        complete {
          (compressor ? GetIndex(index)).mapTo[GetIndexResponse].map(response => response.ch match {
            case Some(c) => c.toString
            case None => "Ups! nothing at this index"
          })
        }
      }
    }

   private def startScheduler(): Unit = {
     system.scheduler.schedule(2.seconds, 60.seconds) {
       runsServiceClient.fetchRuns.onComplete {
         case Success(runs) => compressor ! Compress(runs)
         case Failure(e) => logger.info(s"some error occurred - ${e.getMessage}")
       }
     }
   }

}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val getIndex = jsonFormat1(GetIndex)
  implicit val getIndexResponse = jsonFormat1(GetIndexResponse)
}

