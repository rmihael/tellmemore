package tellmemore

import scala.language.implicitConversions

import org.scala_tools.time.Imports._

package object userfacts {
  sealed abstract class FactValue

  object FactValue {
    def apply(value: String) = StringValue(value)
    def apply(value: Double) = NumericValue(value)

    case class StringValue(value: String) extends FactValue
    case class NumericValue(value: Double) extends FactValue
  }

  type UserFactValues = Map[String, FactValue]

  abstract sealed class UserFact {
    val clientId: String
    val name: String
    val created: DateTime
  }

  object UserFact {
    case class StringFact(clientId: String, name: String, created: DateTime) extends UserFact
    case class NumericFact(clientId: String, name: String, created: DateTime) extends UserFact
  }


}
