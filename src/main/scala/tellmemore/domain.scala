package tellmemore

import org.scala_tools.time.Imports._

case class Client(id: String, name: String, created: DateTime)

case class UserId(clientId: String, externalId: String)

case class User(id: UserId, created: DateTime)

case class Event(userId: UserId, eventName: String, happened: DateTime)

object FactType extends Enumeration {
  type FactType = Value
  val String, Numeric = Value
}
import FactType._

case class UserFact(clientId: String, name: String, factType: FactType, created: DateTime)

sealed abstract class UserFactValue {
  val factType: FactType
}
case class StringFact(value: String) extends UserFactValue {
  val factType = String
}
case class NumericFact(value: Double) extends UserFactValue {
  val factType = Numeric
}

sealed abstract class TimeSpan {
  val start: DateTime
  val end: DateTime
}
case class ExactTimeSpan(start: DateTime, end: DateTime) extends TimeSpan
case class BackwardTimeSpan(period: Period) extends TimeSpan {
  val end = DateTime.now
  val start = end - period
}
