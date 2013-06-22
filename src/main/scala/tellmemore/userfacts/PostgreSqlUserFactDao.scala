package tellmemore.userfacts

import scala.language.postfixOps

import javax.sql.DataSource

import anorm._
import anorm.SqlParser._
import org.scala_tools.time.Imports._

import tellmemore.users.UserId
import tellmemore.infrastructure.DB
import tellmemore.queries.facts.{FactsQuery, FactsQueryAst}

case class PostgreSqlUserFactDao(dataSource: DataSource) extends UserFactDao {
  import UserFact._

  private[this] val simple =
    get[Long]("facts.client_id") ~
    get[Int]("facts.fact_type") ~
    get[String]("facts.fact_name") ~
    get[Long]("facts.created") map {
      case clientId~0~factName~created => Some(StringFact(clientId.toString, factName, new DateTime(created)))
      case clientId~1~factName~created => Some(NumericFact(clientId.toString, factName, new DateTime(created)))
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
      case (sql, (factName, FactValue.NumericValue(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, value, None, tstamp.millis)
      case (sql, (factName, FactValue.StringValue(value))) => sql.addBatchParams(id.clientId, factName, id.externalId, None, value, tstamp.millis)
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }

  def bulkInsert(facts: Set[UserFact]) {
    val insertQuery = SQL(
      """INSERT INTO facts(client_id, fact_type, fact_name, created)
         VALUES((SELECT id FROM clients WHERE email={client_id}), {fact_type}, {fact_name}, {created})""")
    val batchInsert = (insertQuery.asBatch /: facts) {
      (sql, fact) => {
        val factType = fact match {
          case StringFact(_,_,_) => 0
          case NumericFact(_,_,_) => 1
        }
        sql.addBatchParams(fact.clientId, factType, fact.name, fact.created.millis)
      }
    }
    DB.withConnection(dataSource) { implicit connection => batchInsert.execute() }
  }

  def find(query: FactsQuery): Set[String] = {
    // TODO: Achtung! Potential for SQL injection
    val request = ast2sql(query.ast, name2IdMap(query.clientId))
    val users = DB.withConnection(dataSource) { implicit connection => SQL(request).as(userIds *) }
    users.toSet
  }

  private[this] def name2IdMap(clientId: String): Map[String, Long] = DB.withConnection(dataSource) { implicit connection =>
    SQL("""SELECT id, fact_name FROM facts
           WHERE client_id=(SELECT id FROM clients WHERE email={client_id})""")
      .on("client_id" -> clientId)
      .as(factName2Id *)
      .toMap
  }

  private[this] def ast2sql(a: FactsQueryAst, facts: Map[String, Long]): String = {
    import tellmemore.queries.facts.FactsQueryAst._
    a match {
      case NumericEqual(fact, value, moment) => makeSql(facts(fact), moment.tstamp, "numeric_value", value.toString, "=")
      case NumericGreaterThen(fact, value, moment) => makeSql(facts(fact), moment.tstamp, "numeric_value", value.toString, ">")
      case NumericLessThen(fact, value, moment) => makeSql(facts(fact), moment.tstamp, "numeric_value", value.toString, "<")
      case StringEqual(fact, value, moment) => makeSql(facts(fact), moment.tstamp, "string_value", s"'$value'", "=")
      case OrNode(subqueries) => subqueries map {ast2sql(_, facts)} mkString " UNION "
      case AndNode(subqueries) => subqueries map {ast2sql(_, facts)} mkString " INTERSECT "
    }
  }

  private[this] def makeSql(factId: Long, moment: DateTime, column: String, sqlValue: String, operator: String) =
    s"""SELECT external_id FROM users WHERE id IN (
      WITH slice AS (SELECT * FROM fact_values values1 WHERE values1.fact_id=$factId
                     AND values1.tstamp <= ${moment.millis})
      SELECT DISTINCT values1.user_id FROM
        slice AS values1 LEFT JOIN slice AS values2
        ON values1.user_id = values2.user_id AND values1.tstamp < values2.tstamp
      WHERE values2.id IS NULL AND values1.$column $operator $sqlValue
    )"""

}
