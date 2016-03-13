package akkaTipsAndTricks

/** BookCounter is a set of tools for maintaining the running count of Books.*/
object WordCounter {
  import scala.collection.immutable.Map
  
  type Word = String  
  type Count = Int
  
  type InventoryCounter = Map[Word, Count]
  
  val emptyCounter : InventoryCounter = Map.empty[Word, Count]
  
  /**Increments a running counter for the inputed book.*/
  def incrementCounter(counter : InventoryCounter, 
                       book : Word) : InventoryCounter = 
    counter.updated(book, counter.getOrElse(book, 0) + 1)
}


/** Akka wrapping of BookCounter functionality. */
object StreamState {
  import WordCounter._
  import akka.stream.scaladsl.Flow
  
  /** A Flow that keeps a running counter of inputed Books.*/
  val flowCounter : Flow[Word, InventoryCounter, _] = 
    Flow[Word].scan(emptyCounter)(incrementCounter)
}


/** App that takes Book titles through stdin and prints the running counters.*/
object FunctionalState extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem("FunctionalState")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  import actorSystem.dispatcher
  
  import akka.stream.scaladsl.{Source,Sink}
  
  def wordsFromStdin() = io.Source.stdin.getLines.map(_.split(' ')).flatten
  
  Source.fromIterator(wordsFromStdin)
        .via(StreamState.flowCounter)
        .runWith(Sink foreach println)
        .onComplete(_ => actorSystem.terminate)
}