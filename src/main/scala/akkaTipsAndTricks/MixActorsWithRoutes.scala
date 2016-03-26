package akkaTipsAndTricks

import scala.concurrent.duration._
import akka.http.scaladsl.model.{HttpResponse,HttpRequest}
import akka.actor.{Actor, Props, ActorRef}
import akka.util.Timeout
import akka.pattern.ask
import scala.util.{Success, Failure}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http


/**
 * @see http://stackoverflow.com/questions/30930057/akka-http-java-api-respond-with-result-of-an-actor-call/34845060#34845060
 */
class RequestHandlerActor extends Actor {
  override def receive = {
    case _ : HttpRequest =>
      sender() ! HttpResponse(entity = "actor responds nicely")
  }
}

object MixActorsWithRoutes {

  def internalError(ex : Throwable) = 
    complete((InternalServerError, s"Actor not playing nice: ${ex.getMessage}"))
    
  def actorRoute(requestRef : ActorRef)(implicit timeout : Timeout) : Route = 
    extractRequest { request =>
      onComplete((requestRef ? request).mapTo[HttpResponse]) {
        case Success(response) => complete(response)
        case Failure(ex)       => internalError(ex)
      } 
    }
}

object ActorBasedMicroservices extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem()
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  implicit val askTimeout = Timeout(5 seconds)

  val requestRef = actorSystem actorOf Props[RequestHandlerActor]
  
  val route = MixActorsWithRoutes.actorRoute(requestRef)
  
  Http(actorSystem).bindAndHandle(route, interface="localhost", port=42)
}















































