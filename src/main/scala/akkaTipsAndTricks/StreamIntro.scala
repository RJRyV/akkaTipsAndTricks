package akkaTipsAndTricks

import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Route

/**
 * Basic Akka Stream example. 
 */
object StreamIntro extends App {
  
  //boilerplate code for concurrency
  
  implicit val actorSystem = akka.actor.ActorSystem("StreamIntro")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  import actorSystem.dispatcher
  
  //Stream : Source --> 0-N Flows --> Sink
  
  val linesFromStdin = Source fromIterator io.Source.stdin.getLines
  
  //Flow[InputType].map[OutputType](InputType => OutputType)
  
  val strToIntFlow = Flow[String].map[Int](strVal => strVal.toInt)
  
  def multInt(i : Int) = i * 2
  
  val multIntFlow = Flow[Int] map multInt 
  
  val resultSink = Sink.seq[Int]
  
  val seqFut : Future[Seq[Int]] = linesFromStdin.via(strToIntFlow)
                                                .via(multIntFlow)
                                                .runWith(resultSink)

  seqFut onSuccess { case seq =>
    println(s"Sequence is: $seq")
  }

  //Akka HTTP
  
  //  HttpRequest  -->  Route  -->  HttpResponse 
  val httpHandler : Route = 
    (get & path("/mult" / Segment)) { (intAsStr : String) =>
      val intVal = intAsStr.toInt
      complete(HttpResponse(entity=multInt(intVal).toString()))
  }
  
  //The entire server
  Http().bindAndHandle(httpHandler, "localhost", 5600)
  
  
  //client code
  val reqVal = 24
  
  val resp : Future[HttpResponse] = Http().singleRequest(HttpRequest(uri="/mult", entity=s"$reqVal"))

}//end object StreamIntro