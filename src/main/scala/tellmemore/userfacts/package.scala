package tellmemore

import scala.language.implicitConversions

import org.scala_tools.time.Imports._

package object userfacts {
  object FactType extends Enumeration {
    type FactType = Value
    val String, Numeric = Value
  }
  import FactType._

  sealed abstract class FactValue {
    val factType: FactType
  }
  case class StringFact(value: String) extends FactValue {
    val factType = String
  }
  case class NumericFact(value: Double) extends FactValue {
    val factType = Numeric
  }

  case class UserFact(clientId: String, name: String, factType: FactType, created: DateTime)

  type UserFactValues = Map[String, FactValue]

  implicit def double2NumericFact(d: Double): NumericFact = NumericFact(d)
  implicit def string2StringFact(s: String): StringFact = StringFact(s)
}
