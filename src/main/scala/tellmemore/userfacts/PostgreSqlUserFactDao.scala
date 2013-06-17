package tellmemore.userfacts

import scala.language.postfixOps

import javax.sql.DataSource

import anorm._
import anorm.SqlParser._
import org.scala_tools.time.Imports._

import tellmemore.{UserId, UserFact, NumericFact, StringFact, FactType}
import tellmemore.infrastructure.DB
import tellmemore.queries.facts.{FactsQuery, FactsQueryAst}

case class PostgreSqlUserFactDao(dataSource: DataSource) extends UserFactDao {
  private[this] val simple =
    get[Long]("facts.client_id") ~
    get[Short]("facts.fact_type") ~
    get[String]("facts.fact_name") ~
    get[Long]("facts.created") map {
      case clientId~factType~factName~created if factType < FactType.maxId =>
        Some(UserFact(clientId.toString, factName, FactType(factType), new DateTime(created)))
      case _ => None // TODO: Add log about data inconsistency
    }

  private[this] val factName2Id =
    get[Long]("facts.id") ~
    get[String]("facts.fact_name") map {
      case factId~factName => factName -> factId
    }

  private[this] val userIds = get[String]("external_id")

  def getByClientId(clientId: String): Set[UserFact] = DB.withConnection(dataSource) { implicit connection =>
    SQL(
      """SELECT client_id, fact_type, fact_name, created FROM facts
         WHERE client_id=(SELECT id FROM clients WHERE email={client_id})""")
      .on("client_id" -> clientId)
      .as(simple *)
      .flatten // simple parser gives us back Option, not raw object as usual. Flatten will drop None from the list
      .toSet
  }

  def setValuesForUser(id: UserId, values: UserFactValues, tstamp: DateTime) {
    val insertQuery = SQL(
      """INSERT INTO fact_values (fact_id, user_id, numeric_value, string_value, tstamp)
         VALUES ((SELECT id FROM facts WHERE client_id = (SELECT id FROM clients WHERE email = {client_id}) AND fact_name = {fact_name}),
         (SELECT id FROM users WHERE external_id = {user_id}),
         {numeric_value}, {string_value}, {tstamp})""")
    val batchInsert = (insertQuery.asBatch /: values.toSeq) {
      case (sql, (factName, NumericFact(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, value, None, tstamp.millis)
      case (sql, (factName, StringFact(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, None, value, tstamp.millis)
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }

  def bulkInsert(facts: Set[UserFact]) {
    val insertQuery = SQL(
      """INSERT INTO facts(client_id, fact_type, fact_name, created)
         VALUES((SELECT id FROM clients WHERE email={client_id}), {fact_type}, {fact_name}, {created})""")
    val batchInsert = (insertQuery.asBatch /: facts) {
      (sql, fact) => sql.addBatchParams(fact.clientId, fact.factType.id, fact.name, fact.created.millis)
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }

  def find(query: FactsQuery): Set[String] = {
    // TODO: Achtung! Potential for SQL injection
    val sqlAst = queryTranslator(query.ast, name2IdMap(query.clientId))
    val users = DB.withConnection(dataSource) { implicit connection => SQL(sqlAst.sql).as(userIds *) }
    users.toSet
  }

  private[this] def name2IdMap(clientId: String): Map[String, Long] = DB.withConnection(dataSource) { implicit connection =>
    SQL("""SELECT id, fact_name FROM facts
           WHERE client_id=(SELECT id FROM clients WHERE email={client_id})""")
      .on("client_id" -> clientId)
      .as(factName2Id *)
      .toMap
  }
}

private[userfacts] object queryTranslator {
  def apply(ast: FactsQueryAst, facts: Map[String, Long]): SqlAst = {
    import tellmemore.queries.facts.FactsQueryAst.{AndNode, OrNode, Condition}
    ast match {
      case Condition(fact, value) => BasicSql(SqlCondition(facts(fact), value))
      case OrNode(subqueries) => UnionSql(subqueries map {queryTranslator(_, facts)})
      case AndNode(subqueries) => IntersectionSql(subqueries map {queryTranslator(_, facts)})
    }
  }
}
