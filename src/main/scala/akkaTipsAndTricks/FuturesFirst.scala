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
  
                                  
  //Monads are easy!
                                  
  def loginWasActive(loginId : LoginId, 
                     date : Date) : Future[Boolean] = 
    for {
      uniqueId  <- dbLookupLoginToUniqueId(loginId)
      wasActive <- authenticatorLookupIdActive(uniqueId, date)
    } yield (wasActive)
    
  //Composability is very clean
    
  val allIds : Iterable[LoginId] = ???
  
  val someDate : Date = ???
  
  def loginWasActiveOnDate(loginId : LoginId) : Future[Boolean] = 
    loginWasActive(loginId,someDate)
  
  // Iterable(Future[Boolean], Future[Boolean], Future[Boolean], ...)
  val allIdsActive : Iterable[Future[Boolean]] = allIds map loginWasActiveOnDate
    
  // Future[Iterable(Boolean, Boolean, Boolean, ...)
  val idsActive : Future[Iterable[Boolean]] = Future sequence allIdsActive
  
  def bindLoginToUnique(loginId : LoginId) = 
    dbLookupLoginToUniqueId(loginId).map(loginId -> _)
    
  val idsToUniqueIds : Future[Map[LoginId, UniqueId]] = 
    Future.sequence(allIds.map(bindLoginToUnique))
          .map(_.toMap)
          
  
}






































