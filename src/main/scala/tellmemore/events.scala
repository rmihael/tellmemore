package tellmemore.events

import scala.language.postfixOps

import javax.sql.DataSource

import anorm.SqlParser._
import anorm._
import org.scala_tools.time.Imports._
import org.springframework.scala.transaction.support.TransactionManagement

import tellmemore.infrastructure.DB
import tellmemore.{TimeSpan, UserId, Event}
import org.springframework.transaction.PlatformTransactionManager

case class EventModel(eventDao: EventDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getByUserIdAndTimestamp(userId: UserId, timeSpan: TimeSpan): Set[Event] =
    transactional(readOnly = true) { txStatus =>
      eventDao.getByUserIdAndTimestamp(userId, timeSpan.start, timeSpan.end)
    }

  def getAllByClientId(clientId: String): Set[Event] = eventDao.getAllByClientId(clientId)

  def bulkInsert(events: Set[Event]) {
    transactional() { txStatus =>
      eventDao.bulkInsert(events)
    }
  }

  def insert(event: Event) {
    bulkInsert(Set(event))
  }

  def getEventNames(clientId: String): Set[String] = ???
}

trait EventDao {
  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Set[Event]
  def getAllByClientId(clientId: String): Set[Event]
  def bulkInsert(events: Set[Event])
}

case class PostgreSqlEventDao(dataSource: DataSource) extends EventDao {
  private[this] val simple =
      get[Long]("events.client_id") ~
      get[String]("events.external_user_id") ~
      get[String]("events.event_name") ~
      get[Int]("events.tstamp") map {
        case clientId~externalId~eventName~tstamp => Event(UserId(clientId.toString, externalId.toString),
                                                           eventName, new DateTime(tstamp * 1000L))
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
         VALUES({client_id}, {external_user_id}, {event_name}, {tstamp})""")
      val batchInsert = (insertQuery.asBatch /: events) {
        (sql, event) => sql.addBatchParams(event.userId.clientId, event.userId.externalId,
          event.eventName, event.happened.millis / 1000)
      }
      batchInsert.execute()
    }
  }
}
