package tellmemore.events

import scala.language.postfixOps

import javax.sql.DataSource

import anorm.SqlParser._
import anorm._
import org.scala_tools.time.Imports._

import tellmemore.infrastructure.DB
import tellmemore.{UserId, Event}

case class PostgreSqlEventDao(dataSource: DataSource) extends EventDao {
  private[this] val simple =
      get[Long]("events.client_id") ~
      get[String]("events.external_user_id") ~
      get[String]("events.event_name") ~
      get[Long]("events.tstamp") map {
        case clientId~externalId~eventName~tstamp => Event(UserId(clientId.toString, externalId.toString),
                                                           eventName, new DateTime(tstamp))
      }

  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Set[Event] =
    DB.withConnection(dataSource) { implicit connection =>
      SQL("""SELECT id, client_id, external_id, human_readable_id, created FROM users
           WHERE client_id={client_id} AND external_id={external_id}""")
        .on("external_id" -> userId.externalId, "client_id" -> userId.clientId)
        .as(simple *).toSet
    }

  def getAllByClientId(clientId: String): Set[Event] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT id, client_id, external_id, human_readable_id, created FROM users WHERE client_id={client_id}")
      .on("client_id" -> clientId)
      .as(simple *).toSet
  }

  def bulkInsert(events: Set[Event]) {
    DB.withConnection(dataSource) { implicit connection =>
      val insertQuery = SQL(
        """INSERT INTO events(client_id, external_user_id, event_name, tstamp)
         VALUES((SELECT id FROM clients WHERE email={client_id}), {external_user_id}, {event_name}, {tstamp})""")
      val batchInsert = (insertQuery.asBatch /: events) {
        (sql, event) => sql.addBatchParams(event.userId.clientId, event.userId.externalId,
          event.eventName, event.happened.millis)
      }
      batchInsert.execute()
    }
  }

  /**
   * Returns a list of unique event names for this client id.
   * @param clientId
   *                id of client to get events for.
   * @return
   */
  def getEventNames(clientId: String): Set[String] = DB.withConnection(dataSource) { implicit connection =>
    SQL("""SELECT DISTINCT event_name FROM events WHERE client_id=(SELECT id FROM clients WHERE email={cid})""").
      on("cid" -> clientId).
      as(str("event_name") *).toSet
  }
}
