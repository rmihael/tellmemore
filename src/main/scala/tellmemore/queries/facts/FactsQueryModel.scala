package tellmemore.queries.facts

import tellmemore.userfacts.UserFactModel
import play.api.libs.json.{JsValue, Json}
import org.codehaus.jackson.JsonParseException

case class FactsQueryModel(userFactModel: UserFactModel) {
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
    ast map { cond => facts.get(cond.fact) exists { _.factType == cond.value.factType } } forall identity
  }

  def find(clientId: String, ast: FactsQueryAst): Option[Set[String]] = validate(clientId, ast) match {
    case true => Some(userFactModel find FactsQuery(clientId, ast))
    case false => None
  }

  def find(clientId: String, query: String): Option[Set[String]] = try find(clientId, Json.parse(query)) catch {
    case exc:JsonParseException => None
  }

  def find(clientId: String, query: JsValue): Option[Set[String]] = {
    FactsQueryAst parse query match {
      case Right(ast) => find(clientId, ast)
      case Left(_) => None
    }
  }
}
