package tellmemore.queries.facts

import tellmemore.FactValue

private[facts] sealed abstract class FactsQueryAstT[FactT] extends Traversable[FactsQueryAstT.Condition[FactT]] {
  def dependsOn(fact: FactT): Boolean = this.exists {_.fact == fact}
}

private[facts] object FactsQueryAstT {
  case class AndNode[FactT](subqueries: Seq[FactsQueryAstT[FactT]]) extends FactsQueryAstT[FactT] {
    def foreach[U](f: (Condition[FactT]) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class OrNode[FactT](subqueries: Seq[FactsQueryAstT[FactT]]) extends FactsQueryAstT[FactT] {
    def foreach[U](f: (Condition[FactT]) => U) { subqueries foreach { _.foreach(f) }}
  }
  case class Condition[FactT](fact: FactT, value: FactValue) extends FactsQueryAstT[FactT] {
    def foreach[U](f: (Condition[FactT]) => U) { f(this) }
  }
}
