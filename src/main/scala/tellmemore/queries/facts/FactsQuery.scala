package tellmemore.queries.facts

import tellmemore.FactValue
import tellmemore.queries.Moment

case class FactsQuery(clientId: String, ast: FactsQueryAst)

sealed abstract class FactsQueryAst extends Traversable[FactsQueryAst.Condition] {
  def dependsOn(fact: String): Boolean = this.exists {_.fact == fact}
}

object FactsQueryAst {
  case class AndNode(subqueries: Seq[FactsQueryAst]) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class OrNode(subqueries: Seq[FactsQueryAst]) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class Condition(fact: String, value: FactValue, moment: Moment) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { f(this) }
  }
}
