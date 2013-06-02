package tellmemore.userfacts

import tellmemore.{UserId, UserFact}
import org.joda.time.DateTime

trait UserFactDao {
  def getByClientId(clientId: String): Set[UserFact]
  def bulkInsert(facts: Set[UserFact])
  def setValuesForUser(id: UserId, values: UserFactValues, tstamp: DateTime)
}
