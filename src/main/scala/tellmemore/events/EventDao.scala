package tellmemore.events

import tellmemore.{Event, UserId}
import org.joda.time.DateTime

trait EventDao {
  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Set[Event]
  def getAllByClientId(clientId: String): Set[Event]
  def bulkInsert(events: Set[Event])
}
