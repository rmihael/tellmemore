package tellmemore

package object queries {
  sealed abstract class FactsQueryAst[FactType]

  object FactsQueryAst {
    case class AndNode[FactType](subqueries: Seq[FactsQueryAst[FactType]]) extends FactsQueryAst[FactType]
    case class OrNode[FactType](subqueries: Seq[FactsQueryAst[FactType]]) extends FactsQueryAst[FactType]
    case class Condition[FactType](fact: FactType, value: FactValue) extends FactsQueryAst[FactType]
  }
}
