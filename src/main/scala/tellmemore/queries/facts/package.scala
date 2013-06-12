package tellmemore.queries

import play.api.libs.json._
import tellmemore.{NumericFact, StringFact}

package object facts {
  type FactsQueryAst = FactsQueryAstT[String]

  object FactsQueryAst {
    def parse(unparsedQuery: JsValue): Either[Seq[JsValue], FactsQueryAst] = {
      unparsedQuery match {
        case JsObject(Seq(("$and", JsArray(subqueries)))) if subqueries.size >= 2 =>
          val parsedValues = subqueries map {parse(_)}
          processErrors(parsedValues).right map {FactsQueryAstT.AndNode(_)}
        case JsObject(Seq(("$or", JsArray(subqueries)))) if subqueries.size >= 2 =>
          val parsedValues = subqueries map {parse(_)}
          processErrors(parsedValues).right map {FactsQueryAstT.OrNode(_)}
        case JsObject(Seq((factName, JsString(value)))) =>
          Right(FactsQueryAstT.Condition(factName, StringFact(value)))
        case JsObject(Seq((factName, JsNumber(value)))) =>
          Right(FactsQueryAstT.Condition(factName, NumericFact(value.doubleValue())))
        case badValue => Left(Seq(badValue))
      }
    }

    private[this] def processErrors(parsedValues: Seq[Either[Seq[JsValue], FactsQueryAst]]): Either[Seq[JsValue], Seq[FactsQueryAst]] =
      parsedValues.partition {_.isLeft} match {
        case (Nil, queries) => Right(for(Right(query) <- queries) yield query)
        case (errors, _) => Left((for(Left(badValues) <- errors) yield badValues).flatten)
      }

  }
}
