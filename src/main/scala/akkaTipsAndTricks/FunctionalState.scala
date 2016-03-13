package akkaTipsAndTricks

/** BookCounter is a set of tools for maintaining the running count of Books.*/
object BookCounter {
  import scala.collection.immutable.Map
  
  type Book = String  
  type Count = Int
  
  type InventoryCounter = Map[Book, Count]
  
  val emptyCounter : InventoryCounter = Map.empty[Book, Count]
  
  /**Increments a running counter for the inputed book.*/
  def incrementCounter(counter : InventoryCounter, 
                       book : Book) : InventoryCounter = 
    counter.updated(book, counter.getOrElse(book, 0) + 1)
}


/** Akka wrapping of BookCounter functionality. */
object StreamState {
  import BookCounter._
  import akka.stream.scaladsl.Flow
  
  /** A Flow that keeps a running counter of inputed Books.*/
  val flowCounter : Flow[Book, InventoryCounter, _] = 
    Flow[Book].scan(emptyCounter)(incrementCounter)
}


/** App that takes Book titles through stdin and prints the running counters.*/
object FunctionalState extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem("FunctionalState")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  import actorSystem.dispatcher
  
  import akka.stream.scaladsl.{Source,Sink}
  
  Source.fromIterator(io.Source.stdin.getLines)
        .via(StreamState.flowCounter)
        .runWith(Sink foreach println)
        .onComplete(_ => actorSystem.terminate)
}