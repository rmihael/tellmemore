package tellmemore.queries.facts

import play.api.libs.json.{Json, JsValue, JsArray, JsObject, JsString, JsNumber}
import org.codehaus.jackson.JsonParseException

import tellmemore.userfacts.{UserFact, UserFactModel}
import tellmemore.infrastructure.time.TimeProvider
import tellmemore.queries.Moment
import tellmemore.queries.facts.FactsQueryAst._
import org.joda.time.DateTime

case class FactsQueryModel(userFactModel: UserFactModel, timeProvider: TimeProvider) {
  import UserFact._

  val parser = FactsQueryParser(timeProvider)

  def find(clientId: String, ast: FactsQueryAst): Option[Set[String]] = validate(clientId, ast) match {
    case true => Some(userFactModel find FactsQuery(clientId, ast))
    case false => None
  }

  def find(clientId: String, query: String): Option[Set[String]] = try find(clientId, Json.parse(query)) catch {
    case exc:JsonParseException => None
  }

  def find(clientId: String, query: JsValue): Option[Set[String]] = {
    parser(query) match {
      case Right(ast) => find(clientId, ast)
      case Left(_) => None
    }
  }

  /**
   * This method performs validation on facts query. Currently validation is consisting of two steps:
   * 1. All facts referenced in query must be present in client facts registry
   * 2. Every variable in query's condition must have type compatible with corresponding fact
   * @param clientId id of the client whom data will be user for validation
   * @param ast query AST to validate versus client data
   * @return if all validation steps pass then `true` is returned. `false` otherwise.
   */
  private[this] def validate(clientId: String, ast: FactsQueryAst): Boolean = {
    val facts = userFactModel.getUserFactsForClient(clientId)
    ast map {cond => cond -> facts.get(cond.fact)} forall {
      case (StringEqual(_,_,_), Some(StringFact(_,_,_))) => true
      case (NumericEqual(_,_,_), Some(NumericFact(_,_,_))) => true
      case (NumericGreaterThen(_,_,_), Some(NumericFact(_,_,_))) => true
      case (NumericLessThen(_,_,_), Some(NumericFact(_,_,_))) => true
      case _ => false
    }
  }
}

private[facts] case class FactsQueryParser(timeProvider: TimeProvider) {
  def apply(unparsedQuery: JsValue): Either[Seq[JsValue], FactsQueryAst] = {
    val now = timeProvider.now
    unparsedQuery match {
      case JsObject(Seq(("$and", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {this(_)}
        processErrors(parsedValues).right map {FactsQueryAst.AndNode(_)}
      case JsObject(Seq(("$or", JsArray(subqueries)))) if subqueries.size >= 2 =>
        val parsedValues = subqueries map {this(_)}
        processErrors(parsedValues).right map {FactsQueryAst.OrNode(_)}
      case JsObject(Seq((factName, c))) => parseCondition(factName, c, now)
      case badValue => Left(Seq(badValue))
    }
  }

  private[this] def parseCondition(factName: String, condition: JsValue, now: DateTime) = condition match {
    case JsString(value) => Right(StringEqual(factName, value, Moment.Now(now)))
    case JsObject(Seq(("$eq", JsString(value)))) => Right(StringEqual(factName, value, Moment.Now(now)))
    case JsNumber(value) => Right(NumericEqual(factName, value.doubleValue(), Moment.Now(now)))
    case JsObject(Seq(("$eq", JsNumber(value)))) => Right(NumericEqual(factName, value.doubleValue(), Moment.Now(now)))
    case JsObject(Seq(("$eq", JsNumber(value)), ("$at", JsNumber(moment)))) =>
      Right(NumericEqual(factName, value.doubleValue(), Moment.Timestamp(new DateTime(moment))))
    case JsObject(Seq(("$gt", JsNumber(value)))) => Right(NumericGreaterThen(factName, value.doubleValue(), Moment.Now(now)))
    case JsObject(Seq(("$gt", JsNumber(value)), ("$at", JsNumber(moment)))) =>
      Right(NumericGreaterThen(factName, value.doubleValue(), Moment.Timestamp(new DateTime(moment))))
    case JsObject(Seq(("$lt", JsNumber(value)))) => Right(NumericLessThen(factName, value.doubleValue(), Moment.Now(now)))
    case JsObject(Seq(("$lt", JsNumber(value)), ("$at", JsNumber(moment)))) =>
      Right(NumericLessThen(factName, value.doubleValue(), Moment.Timestamp(new DateTime(moment))))
    case badValue => Left(Seq(badValue))
  }

  private[this] def processErrors(parsedValues: Seq[Either[Seq[JsValue], FactsQueryAst]]): Either[Seq[JsValue], Seq[FactsQueryAst]] =
    parsedValues.partition {_.isLeft} match {
      case (Nil, queries) => Right(for(Right(query) <- queries) yield query)
      case (errors, _) => Left((for(Left(badValues) <- errors) yield badValues).flatten)
    }
}
