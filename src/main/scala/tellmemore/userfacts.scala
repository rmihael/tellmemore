package tellmemore.userfacts

import scala.language.postfixOps

import javax.sql.DataSource

import org.springframework.scala.transaction.support.TransactionManagement
import org.springframework.transaction.PlatformTransactionManager
import anorm._
import anorm.SqlParser._
import org.scala_tools.time.Imports._

import tellmemore.infrastructure.{TimeProvider, DB}
import tellmemore._
import scala.Some
import tellmemore.UserId
import tellmemore.UserFact
import anorm._

case class UserFactModel(userFactDao: UserFactDao,
                         transactionManager: PlatformTransactionManager,
                         timeProvider: TimeProvider) extends TransactionManagement {
  def getUserFactsForClient(clientId: String): Set[UserFact] = transactional() { txStatus =>
    userFactDao.getByClientId(clientId)
  }

  def setForUser(id: UserId, values: Map[String, UserFactValue]): Either[Map[String, UserFactValue], Int] =
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

  private[this] def detectBrokenValues(values: Map[String, UserFactValue], facts: Set[UserFact]): Set[UserFact] = {
    val factsMap = (facts map {fact => fact.name -> fact}).toMap
    val badFacts = values collect {
      case (name, v) if factsMap.get(name) map {fact => v.factType != fact.factType} getOrElse(false) => factsMap(name)
    }
    badFacts.toSet
  }
}

trait UserFactDao {
  def getByClientId(clientId: String): Set[UserFact]
  def bulkInsert(facts: Set[UserFact])
  def setValuesForUser(id: UserId, values: Map[String, UserFactValue], tstamp: DateTime)
}

case class PostgreSqlUserFactDao(dataSource: DataSource) extends UserFactDao {
  private[this] val simple =
    get[Long]("facts.client_id") ~
    get[Short]("facts.fact_type") ~
    get[String]("facts.fact_name") ~
    get[Int]("facts.created") map {
      case clientId~factType~factName~created if factType < FactType.maxId =>
        Some(UserFact(clientId.toString, factName, FactType(factType), new DateTime(created * 1000L)))
      case _ => None // TODO: Add log about data inconsistency
    }

  def getByClientId(clientId: String): Set[UserFact] = DB.withConnection(dataSource) { implicit connection =>
    SQL(
      """SELECT client_id, fact_type, fact_name, created FROM facts
         WHERE client_id=(SELECT id FROM clients WHERE email={client_id})""")
      .on("client_id" -> clientId)
      .as(simple *)
      .flatten // simple parser gives us back Option, not raw object as usual. Flatten will drop None from the list
      .toSet
  }

  def setValuesForUser(id: UserId, values: Map[String, UserFactValue], tstamp: DateTime) {
    val tstampMillis = tstamp.millis / 1000
    val insertQuery = SQL(
      """INSERT INTO fact_values (fact_id, user_id, numeric_value, string_value, tstamp)
         VALUES ((SELECT id FROM facts WHERE client_id = (SELECT id FROM clients WHERE email = {client_id}) AND fact_name = {fact_name}),
         (SELECT id FROM users WHERE external_id = {user_id}),
         {numeric_value}, {string_value}, {tstamp})""")
    val batchInsert = (insertQuery.asBatch /: values.toSeq) {
      case (sql, (factName, NumericFact(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, value, None, tstampMillis)
      case (sql, (factName, StringFact(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, None, value, tstampMillis)
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }

  def bulkInsert(facts: Set[UserFact]) {
    val insertQuery = SQL(
      """INSERT INTO facts(client_id, fact_type, fact_name, created)
         VALUES((SELECT id FROM clients WHERE email={client_id}), {fact_type}, {fact_name}, {created})""")
    val batchInsert = (insertQuery.asBatch /: facts) {
      (sql, fact) => sql.addBatchParams(fact.clientId, fact.factType.id, fact.name, fact.created.millis / 1000)
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }
}
