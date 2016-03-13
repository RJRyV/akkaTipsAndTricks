package akkaTipsAndTricks

import akka.stream.UniformFanOutShape
import akka.stream.scaladsl.{Source, Flow, Sink, RunnableGraph, Broadcast, MergePreferred,GraphDSL}
import scala.concurrent.Future
import akka.NotUsed
import akka.stream.ClosedShape

/**
 * @see http://stackoverflow.com/questions/32459329/why-akka-streams-cycle-doesnt-end-in-this-graph/33962702#33962702
 */
 object MixedBusinessLogic {
	val ignore = Sink.ignore

  val closed = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
    import GraphDSL.Implicits._

    type FileInputType = (Int, Array[String])
    
    val emptyInputType = (0, Array.empty[String])
    
		val fileSource = Source.single(emptyInputType)
			
    val merge = 
      builder.add(MergePreferred[FileInputType](1, true).named("merge"))
			
    val afterMerge = Flow[FileInputType] map {
			e =>
			println("after merge")
			e
		}
			
    val broadcast = 
      builder.add(Broadcast[FileInputType](2).named("broadcastArray"))
			
    val toRetry = Flow[FileInputType].filter {
			case (r, s) => {
	  		println(s"retry ${(r < 3)} $r")
				r < 3
			}
		}.map {
			case (r, s) => (r + 1, s)
		}
			
    val filterFileInputs = Flow[FileInputType] filter {
		  case (r, s) => {
			  println(s"sink ${(r >= 3)} $r")
				r >= 3
			}
		}
					
    fileSource ~> merge ~> afterMerge ~> broadcast ~> filterFileInputs ~> ignore
                  merge <~  toRetry   <~ broadcast
    ClosedShape
  })
}


object SeperateBusinessLogic {
  type FileInputType = (Int, Array[String])

  val emptyInputType = (0, Array.empty[String])
  
  @scala.annotation.tailrec
  def recursiveRetry(fileInput : FileInputType) : FileInputType = 
    fileInput match { 
      case (r,_) if r >= 3  => fileInput
      case (r,a)            => recursiveRetry((r+1, a))
    }    
}


object AkkaExtension {
  import SeperateBusinessLogic._
  
  val stream = Source.single(emptyInputType) 
                     .via(Flow[FileInputType] map recursiveRetry)
                     .to(Sink.ignore)
}
