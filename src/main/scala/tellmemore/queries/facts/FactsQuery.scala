package tellmemore.queries.facts

import tellmemore.{NumericFact, StringFact, FactValue}
import play.api.libs.json._
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString

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
  case class Condition(fact: String, value: FactValue) extends FactsQueryAst {
    def foreach[U](f: (Condition) => U) { f(this) }
  }

  def parse(unparsedQuery: JsValue): Either[Seq[JsValue], FactsQueryAst] = {
    unparsedQuery match {
      case JsObject(Seq(("$and", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {parse(_)}
        processErrors(parsedValues).right map {FactsQueryAst.AndNode(_)}
      case JsObject(Seq(("$or", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {parse(_)}
        processErrors(parsedValues).right map {FactsQueryAst.OrNode(_)}
      case JsObject(Seq((factName, JsString(value)))) =>
        Right(FactsQueryAst.Condition(factName, StringFact(value)))
      case JsObject(Seq((factName, JsNumber(value)))) =>
        Right(FactsQueryAst.Condition(factName, NumericFact(value.doubleValue())))
      case badValue => Left(Seq(badValue))
    }
  }

  private[this] def processErrors(parsedValues: Seq[Either[Seq[JsValue], FactsQueryAst]]): Either[Seq[JsValue], Seq[FactsQueryAst]] =
    parsedValues.partition {_.isLeft} match {
      case (Nil, queries) => Right(for(Right(query) <- queries) yield query)
      case (errors, _) => Left((for(Left(badValues) <- errors) yield badValues).flatten)
    }

}
