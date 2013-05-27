package tellmemore.events

import scala.language.postfixOps

import javax.sql.DataSource

import anorm.SqlParser._
import anorm._
import org.scala_tools.time.Imports._

import tellmemore.infrastructure.DB

import tellmemore.{TimeSpan, UserId, Event}

case class EventModel(eventDao: EventDao) {
  def getByUserIdAndTimestamp(userId: UserId, timeSpan: TimeSpan): Either[Exception, Option[Event]] =
    eventDao.getByUserIdAndTimestamp(userId, timeSpan.start, timeSpan.end)

  def getAllByClientId(clientId: String): Either[Exception, Seq[Event]] = eventDao.getAllByClientId(clientId)

  def bulkInsert(events: Seq[Event]): Either[Exception, Seq[Event]] = eventDao.bulkInsert(events)

  def insert(user: Event): Either[Exception, Option[Event]] = bulkInsert(Seq(user)).right map {_.headOption}
}

trait EventDao {
  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Either[Exception, Option[Event]]
  def getAllByClientId(clientId: String): Either[Exception, Seq[Event]]
  def bulkInsert(events: Seq[Event]): Either[Exception, Seq[Event]]
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

  def getByUserIdAndTimestamp(userId: UserId, start: DateTime, end: DateTime): Either[Exception, Option[Event]] = {
    try {
      Right(DB.withConnection(dataSource) { implicit connection =>
        SQL("""SELECT id, client_id, external_id, human_readable_id, created FROM users
               WHERE client_id={client_id} AND external_id={external_id}""")
          .on("external_id" -> userId.externalId, "client_id" -> userId.clientId)
          .as(simple.singleOpt)
      })
    } catch {
      case exc: Exception => Left(exc)
    }
  }

  def getAllByClientId(clientId: String): Either[Exception, Seq[Event]] = {
    try {
      Right(DB.withConnection(dataSource) { implicit connection =>
        SQL("SELECT id, client_id, external_id, human_readable_id, created FROM users WHERE client_id={client_id}")
          .on("client_id" -> clientId)
          .as(simple *)
      })
    } catch {
      case exc: Exception => Left(exc)
    }
  }

  def bulkInsert(events: Seq[Event]): Either[Exception, Seq[Event]] = {
    try {
      Right(DB.withConnection(dataSource) { implicit connection =>
        val insertQuery = SQL(
          """INSERT INTO events(client_id, external_user_id, event_name, tstamp)
             VALUES({client_id}, {external_user_id}, {event_name}, {tstamp})""")
        val batchInsert = (insertQuery.asBatch /: events) {
          (sql, event) => sql.addBatchParams(event.userId.clientId, event.userId.externalId,
                                            event.eventName, event.tstamp.millis / 1000)
        }
        batchInsert.execute()
        events
      })
    } catch {
      case exc: Exception => Left(exc)
    }
  }
}
