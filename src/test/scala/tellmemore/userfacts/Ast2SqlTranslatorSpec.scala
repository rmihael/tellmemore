package tellmemore.userfacts

import org.specs2.mutable.Specification

import tellmemore.queries.facts.FactsQueryAst.{AndNode, OrNode, Condition}
import tellmemore.queries.Moment
import org.joda.time.DateTime

class Ast2SqlTranslatorSpec extends Specification {
  "AST to SQL translator" should {
//    "translate deep AST" in {
//      val ast = AndNode(Seq(
//                  OrNode(Seq(Condition("fact", NumericFact(2.5)), Condition("fact2", StringFact("string")))),
//                  AndNode(Seq(Condition("fact3", NumericFact(5.5)), Condition("fact4", StringFact("string2"))))
//                ))
//      val result = "SELECT DISTINCT user_id FROM fact_values WHERE "
//    }

    val facts = Map("numeric_fact" -> 1L, "string_fact" -> 2L)
    val now = DateTime.now

    "translate single numeric condition AST to simple SELECT" in {
      val ast = Condition("numeric_fact", NumericFact(10.0), Moment.Now(now))
      val result = BasicSql(SqlCondition(1, NumericFact(10.0), now))
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate single numeric condition AST to simple SELECT" in {
      val ast = Condition("string_fact", StringFact("somevalue"), Moment.Now(now))
      val result = BasicSql(SqlCondition(2, StringFact("somevalue"), now))
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate 'or' node to SQL UNION" in {
      val ast = OrNode(Seq(Condition("numeric_fact", NumericFact(10.0), Moment.Now(now)),
                           Condition("string_fact", StringFact("somevalue"), Moment.Now(now))))
      val result = UnionSql(Seq(
        BasicSql(SqlCondition(1, NumericFact(10.0), now)),
        BasicSql(SqlCondition(2, StringFact("somevalue"), now))
      ))
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate 'and' node AST to SQL INTERSECTION" in {
      val ast = AndNode(Seq(Condition("numeric_fact", NumericFact(10.0), Moment.Now(now)),
                            Condition("string_fact", StringFact("somevalue"), Moment.Now(now))))
      val result = IntersectionSql(Seq(
        BasicSql(SqlCondition(1, NumericFact(10.0), now)),
        BasicSql(SqlCondition(2, StringFact("somevalue"), now))
      ))
      queryTranslator(ast, facts) must equalTo(result)
    }
  }
}
