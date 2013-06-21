package tellmemore.userfacts

import tellmemore.UserId
import org.joda.time.DateTime
import tellmemore.queries.facts.FactsQuery

trait UserFactDao {
  /**
   * This method returns registry of all user facts for the client specified
   * @param clientId id of the client to get facts
   * @return set of user facts
   */
  def getByClientId(clientId: String): Set[UserFact]

  /**
   * This method inserts new facts for client.
   * @note this method will fail on trying to insert any duplicate of existing fact. In this case no facts will be
   *       inserted, even non-duplicated ones
   * @param facts set of user facts to add to client's registry
   */
  def bulkInsert(facts: Set[UserFact])

  /**
   * This function sets fact values for specific user
   *
   * @param id id of user for whom to set fact values
   * @param values set of fact values
   * @param tstamp the moment in time corresponding to the values provided
   */
  def setValuesForUser(id: UserId, values: UserFactValues, tstamp: DateTime)

  /**
   * This function takes facts query and returns users who are satisfying it
   * @param query query to process
   * @return set of user ids
   */
  def find(query: FactsQuery): Set[String]
}
