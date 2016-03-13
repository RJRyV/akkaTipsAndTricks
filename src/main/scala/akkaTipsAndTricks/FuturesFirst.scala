package akkaTipsAndTricks

import scala.concurrent.Future
import java.util.Date

/**
 * Always consider Futures first.  Streams take a signification performances
 * hit from backpressure.
 * 
 * @see http://stackoverflow.com/questions/33416891/akka-stream-implementation-slower-than-single-threaded-implementation
 */
object FuturesFirst {
  import scala.concurrent.ExecutionContext.Implicits._
  
  type LoginId = String
  type UniqueId = java.util.UUID
  
  def dbLookupLoginToUniqueId(loginId : LoginId) : Future[UniqueId] = ???
  
  def authenticatorLookupIdActive(uniqueId : UniqueId, 
                                  date : Date) : Future[Boolean]= ???
  
                                  
  //Futures can be chained together in many visually distinct ways
                                  
  def loginWasAlsoActive(loginId : LoginId, 
                         date : Date) : Future[(UniqueId, Boolean)] = 
    for {
      uniqueId  <- dbLookupLoginToUniqueId(loginId)
      wasActive <- authenticatorLookupIdActive(uniqueId, date)
    } yield (uniqueId -> wasActive)
                                  
  def loginWasActive(loginId : LoginId, date : Date) : Future[Boolean] = 
    dbLookupLoginToUniqueId(loginId).flatMap(authenticatorLookupIdActive(_, date))
    
    
  //Composability is very clean
    
  val allIds : Iterable[LoginId] = ???
  
  val yesterday = new Date(System.currentTimeMillis()-7*24*60*60*1000)
  
  //The allIds.map spawns off as many Futures as there are ids
  //But the return type is a single Future wrapped around an Iterable of results
  val idsActive : Future[Iterable[Boolean]] = 
    Future.sequence(allIds.map(loginWasActive(_, yesterday)))
  
  def bindLoginToUnique(loginId : LoginId) = 
    dbLookupLoginToUniqueId(loginId).map(loginId -> _)
    
  val idsToUniqueIds : Future[Map[LoginId, UniqueId]] = 
    Future.sequence(allIds.map(bindLoginToUnique))
          .map(_.toMap)
}