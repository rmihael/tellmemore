package tellmemore.events

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement
import tellmemore.{UserId, TimeSpan, Event}

case class EventModel(eventDao: EventDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getByUserIdAndTimestamp(userId: UserId, timeSpan: TimeSpan): Set[Event] =
    transactional(readOnly = true) { txStatus =>
      eventDao.getByUserIdAndTimestamp(userId, timeSpan.start, timeSpan.end)
    }

  def getAllByClientId(clientId: String): Set[Event] = eventDao.getAllByClientId(clientId)

  def bulkInsert(events: Set[Event]) {
    transactional() { txStatus =>
      eventDao.bulkInsert(events)
    }
  }

  def insert(event: Event) {
    bulkInsert(Set(event))
  }

  /**
   * Returns a set of event names available for this client.
   * @param clientId
   *                id of client for which this set must be returned
   * @return
   *         unique set of events names that user has available
   */
  def getEventNames(clientId: String): Set[String] = eventDao.getEventNames(clientId)
}
