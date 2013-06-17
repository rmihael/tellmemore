package tellmemore.userfacts

import scala.language.postfixOps

import org.springframework.scala.transaction.support.TransactionManagement
import org.springframework.transaction.PlatformTransactionManager

import tellmemore.{UserId, UserFact}
import tellmemore.infrastructure.time.TimeProvider
import tellmemore.queries.facts.FactsQuery
import scala.util.Try
import scala.util.control.NonFatal

case class UserFactModel(userFactDao: UserFactDao,
                         transactionManager: PlatformTransactionManager,
                         timeProvider: TimeProvider) extends TransactionManagement {
  /**
   * This method returns registry of all client facts for users
   * @param clientId client it to whom fetch facts
   * @return mapping between fact name and UserFact object
   */
  def getUserFactsForClient(clientId: String): Map[String, UserFact] = transactional() { txStatus =>
    (userFactDao.getByClientId(clientId) map {
      fact => fact.name -> fact
    }).toMap
  }

  /**
   * This method updates current value of user facts with provided values
   * @note if fact with name provided was not found among existing facts then new fact will be created automatically
   *       with type set accordingly to associated value
   * @note if type of provided fact value is different from existing fact's type then method will fail. In that case
   *       none changes are made, even for facts with correct names
   * @param id id of user to update with new fact values
   * @param values mapping between fact name and fact value
   * @return if method succeeds then return value would be Right with number of facts updated. If there were any
   *         incompatibilities between fact values and existing fact types then return value would be Left with
   *         mapping between fact names and fact values that were found incompatible.
   * @example setForUser(userId, Map("string fact" -> StringFact("string value"), "numeric fact" -> NumericFact(1.0)))
   */
  def setForUser(id: UserId, values: UserFactValues): Either[UserFactValues, Int] =
    transactional() { txStatus =>
      val facts = getUserFactsForClient(id.clientId)
      detectBrokenValues(values, facts) match {
        case brokenFacts if brokenFacts.size == 0 => {
          val now = timeProvider.now
          val absentNames = getAbsentNames(values.keySet, facts.values)
          val absentFacts = absentNames map {name => UserFact(id.clientId, name, values(name).factType, now)}
          userFactDao.bulkInsert(absentFacts)
          userFactDao.setValuesForUser(id, values, now)
          Right(values.size)
        }
        case brokenFacts => {
          Left(brokenFacts map {fact => fact.name -> values(fact.name)} toMap)
        }
      }
    }

  /**
   * This method processes user facts query and returns set of user domain ids
   * @param query query object
   * @return set of user ids
   */
  def find(query: FactsQuery): Set[String] = userFactDao.find(query)

  private[this] def getAbsentNames(names: Set[String], facts: Iterable[UserFact]) = names diff (facts map {_.name} toSet)

  private[this] def detectBrokenValues(values: UserFactValues, facts: Map[String, UserFact]): Set[UserFact] = {
    val badFacts = values collect {
      case (name, v) if facts.get(name) exists {fact => v.factType != fact.factType} => facts(name)
    }
    badFacts.toSet
  }
}
