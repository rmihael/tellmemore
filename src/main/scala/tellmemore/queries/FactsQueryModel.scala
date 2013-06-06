package tellmemore.queries

import play.api.libs.json.{JsString, JsValue, JsObject, JsArray, JsNumber}

import tellmemore.{NumericFact, StringFact}

class FactsQueryModel {
}

object FactsQueryModel {
  type BasicFactsQueryAst = FactsQueryAst[String]
  type BasicParsingResult = Either[Seq[JsValue], BasicFactsQueryAst]

  private[queries] def parseJson(unparsedQuery: JsValue): BasicParsingResult = {
    unparsedQuery match {
      case JsObject(Seq(("$and", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {parseJson(_)}
        processErrors(parsedValues).right map {FactsQueryAst.AndNode(_)}
      case JsObject(Seq(("$or", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {parseJson(_)}
        processErrors(parsedValues).right map {FactsQueryAst.OrNode(_)}
      case JsObject(Seq((factName, JsString(value)))) =>
        Right(FactsQueryAst.Condition(factName, StringFact(value)))
      case JsObject(Seq((factName, JsNumber(value)))) =>
        Right(FactsQueryAst.Condition(factName, NumericFact(value.doubleValue())))
      case badValue => Left(Seq(badValue))
    }
  }

  private[this] def processErrors(parsedValues: Seq[BasicParsingResult]): Either[Seq[JsValue], Seq[BasicFactsQueryAst]] =
    parsedValues.partition {_.isLeft} match {
      case (Nil, queries) => Right(for(Right(query) <- queries) yield query)
      case (errors, _) => Left((for(Left(badValues) <- errors) yield badValues).flatten)
    }
}
