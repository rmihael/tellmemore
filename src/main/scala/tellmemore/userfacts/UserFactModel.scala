package tellmemore.userfacts

import scala.language.postfixOps

import org.springframework.scala.transaction.support.TransactionManagement
import org.springframework.transaction.PlatformTransactionManager

import tellmemore.{UserId, UserFact}
import tellmemore.infrastructure.time.TimeProvider

case class UserFactModel(userFactDao: UserFactDao,
                         transactionManager: PlatformTransactionManager,
                         timeProvider: TimeProvider) extends TransactionManagement {
  def getUserFactsForClient(clientId: String): Set[UserFact] = transactional() { txStatus =>
    userFactDao.getByClientId(clientId)
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
          val absentNames = getAbsentNames(values.keySet, facts)
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

  private[this] def getAbsentNames(names: Set[String], facts: Set[UserFact]) = names diff (facts map {_.name})

  private[this] def detectBrokenValues(values: UserFactValues, facts: Set[UserFact]): Set[UserFact] = {
    val factsMap = (facts map {fact => fact.name -> fact}).toMap
    val badFacts = values collect {
      case (name, v) if factsMap.get(name) map {fact => v.factType != fact.factType} getOrElse(false) => factsMap(name)
    }
    badFacts.toSet
  }
}
