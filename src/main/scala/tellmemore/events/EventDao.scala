package tellmemore.events

import tellmemore.{Event}
import tellmemore.users.UserId
import org.joda.time.DateTime

trait EventDao {
  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Set[Event]
  def getAllByClientId(clientId: String): Set[Event]
  def bulkInsert(events: Set[Event])
  def getEventNames(clientId: String): Set[String]
}
