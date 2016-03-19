package akkaTipsAndTricks

import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import scala.concurrent.Future

/**
 * Basic Akka Stream example. 
 */
object StreamIntro extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem("StreamIntro")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  import actorSystem.dispatcher
  
  def linesFromStdin() : Iterator[String] = io.Source.stdin.getLines
  
  //Flow[InputType].map[OutputType](InputType => OutputType)
  
  val strToIntFlow = Flow[String].map[Int](strVal => strVal.toInt)
  
  val multInt = Flow[Int].map(_ * 2) //equivalent to x => x * 2
  
  val resultSink = Sink.seq[Int]
  
  val seqFut : Future[Seq[Int]] = Source.fromIterator(linesFromStdin)
                                        .via(strToIntFlow)
                                        .via(multInt)
                                        .runWith(resultSink)

  seqFut onSuccess { case seq =>
    println(s"Sequence is: $seq")
    actorSystem.terminate()
  }

}//end object StreamIntro