package tellmemore

import org.scala_tools.time.Imports._

case class ClientData(name: String, created: DateTime, lastLogin: DateTime)

case class Client(id: String, name: String, created: DateTime, lastLogin: DateTime)

case class UserId(clientId: String, externalId: String)

case class User(id: UserId, humanReadableId: String, created: DateTime)

case class Event(userId: UserId, eventName: String, tstamp: DateTime)

sealed abstract class TimeSpan {
  val start: DateTime
  val end: DateTime
}
case class ExactTimeSpan(start: DateTime, end: DateTime) extends TimeSpan
case class BackwardTimeSpan(period: Period) extends TimeSpan {
  val end = DateTime.now
  val start = end - period
}
