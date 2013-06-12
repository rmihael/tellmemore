package tellmemore.queries.facts

import tellmemore.{User, UserFact, NumericFact, StringFact}
import tellmemore.userfacts.UserFactModel

case class FactsQueryModel(userFactModel: UserFactModel) {
  /**
   * This method performs validation on facts query. Currently validation is consisting of two steps:
   * 1. All facts referenced in query must be present in client facts registry
   * 2. Every variable in query's condition must have type compatible with corresponding fact
   * @param query query to validate
   * @return if all validation steps pass then `true` is returned. `false` otherwise.
   */
  def validate(query: FactsQuery): Boolean = {
    val facts = userFactModel.getUserFactsForClient(query.clientId)
    query.ast map { cond => facts.get(cond.fact) exists { _.factType == cond.value.factType } } forall identity
  }

  def process(query: FactsQuery): Set[User] = ???
}
