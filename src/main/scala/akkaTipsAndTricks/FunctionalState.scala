package akkaTipsAndTricks

/**
 * BookCounter is a collection of tools for maintaining the running count
 * of Books.
 */
object BookCounter {
  //keep imports local
  import scala.collection.immutable.Map
  
  type Book = String  
  type Count = Int
  
  type InventoryCounter = Map[Book, Count]
  
  /**Creates an empty counter.*/
  val emptyCounter : InventoryCounter = Map.empty[Book, Count]
  
  /**Increments a running counter for the inputed book.*/
  def incrementCounter(counter : InventoryCounter, 
                       book : Book) : InventoryCounter = 
    counter.updated(book, counter.getOrElse(book, 0) + 1)
}//end object Counter

/**
 * Akka wrapping of BookCounter functionality.
 */
object StreamState {
  import BookCounter._
  import akka.stream.scaladsl.Flow
  
  /**
   * A Flow that keeps a running counter of inputed Books.
   * @note Flows are usually vals not defs
   */
  val flowCounter : Flow[Book, InventoryCounter, _] = 
    Flow[Book].scan(emptyCounter)(incrementCounter)
}//end object StreamState

/**
 * Application that takes Book titles through stdin and outputs the 
 * running counters of the inputed titles. 
 */
object FunctionalState extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem("FunctionalState")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  
  akka.stream.scaladsl.Source.fromIterator(io.Source.stdin.getLines)
                             .via(StreamState.flowCounter)
                             .runForeach(println)
}//end object FunctionalState extends App