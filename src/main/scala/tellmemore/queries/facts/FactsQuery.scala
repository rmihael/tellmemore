package tellmemore.queries.facts

import tellmemore.userfacts.{StringFact, NumericFact, FactValue}
import tellmemore.queries.Moment

case class FactsQuery(clientId: String, ast: FactsQueryAst)

sealed abstract class FactsQueryAst extends Traversable[Condition] {
  def dependsOn(fact: String): Boolean = this.exists {_.fact == fact}
}

sealed trait Condition extends FactsQueryAst {
  val fact: String
  val value: FactValue
  val moment: Moment
}

object FactsQueryAst {
  case class AndNode(subqueries: Seq[FactsQueryAst]) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class OrNode(subqueries: Seq[FactsQueryAst]) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class NumericGreaterThen(fact: String, value: NumericFact, moment: Moment) extends Condition {
    def foreach[U](f: (Condition) => U) { f(this) }

    override val toString = s"NumericGreaterThen($fact, $value, $moment)"
  }
  case class NumericLessThen(fact: String, value: NumericFact, moment: Moment) extends Condition {
    def foreach[U](f: (Condition) => U) { f(this) }

    override val toString = s"NumericLessThen($fact, $value, $moment)"
  }
  case class NumericEqual(fact: String, value: NumericFact, moment: Moment) extends Condition {
    def foreach[U](f: (Condition) => U) { f(this) }

    override val toString = s"NumericEqual($fact, $value, $moment)"
  }
  case class StringEqual(fact: String, value: StringFact, moment: Moment) extends Condition {
    def foreach[U](f: (Condition) => U) { f(this) }

    override val toString = s"StringEqual($fact, $value, $moment)"
  }

}
