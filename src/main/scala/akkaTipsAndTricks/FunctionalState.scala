package akkaTipsAndTricks

/** WordCounter is a set of tools for maintaining the running count of words.*/
object WordCounter {
  import scala.collection.immutable.Map
  
  type Word = String  
  type Count = Int
  
  type WordCounter = Map[Word, Count]
  
  val emptyCounter : WordCounter = Map.empty[Word, Count]
  
  /**Increments a running counter for the inputed book.*/
  def incrementCounter(counter : WordCounter, word : Word) : WordCounter = 
    counter.updated(word, counter.getOrElse(word, 0) + 1)
    
}


/** Akka wrapping of WordCounter functionality. */
object StreamState {
  import WordCounter._
  import akka.stream.scaladsl.Flow
  
  /** A Flow that keeps a running counter of inputed Books.*/
  val flowCounter : Flow[Word, WordCounter, _] = 
    Flow[Word].scan(emptyCounter)(incrementCounter)
}


/** App that takes words through stdin and prints the running counters.*/
object FunctionalState extends App {
  
  implicit val actorSystem = akka.actor.ActorSystem("FunctionalState")
  implicit val actorMaterializer = akka.stream.ActorMaterializer()
  import actorSystem.dispatcher
  
  import akka.stream.scaladsl.{Source,Sink}
  
  def wordsFromStdin() = io.Source.stdin.getLines.map(_.split("\\s+")).flatten
  
  Source.fromIterator(wordsFromStdin)
        .via(StreamState.flowCounter)
        .runWith(Sink foreach println)
        .onComplete(_ => actorSystem.terminate)
}














































