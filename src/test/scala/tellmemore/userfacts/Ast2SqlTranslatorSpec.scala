package tellmemore.userfacts

import org.specs2.mutable.Specification

import tellmemore.queries.facts.FactsQueryAst._
import tellmemore.queries.Moment
import org.joda.time.DateTime
import tellmemore.queries.facts.FactsQueryAst.OrNode
import tellmemore.queries.facts.FactsQueryAst.AndNode
import tellmemore.queries.facts.FactsQueryAst.NumericEqual

class Ast2SqlTranslatorSpec extends Specification {
  import SqlAst._

  "AST to SQL translator" should {
    val facts = Map("numeric_fact" -> 1L, "string_fact" -> 2L)
    val now = DateTime.now

    "translate single numeric condition AST to simple SELECT" in {
      val ast = NumericEqual("numeric_fact", NumericFact(10.0), Moment.Now(now))
      val result = SqlNumericEquals(1, NumericFact(10.0), now)
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate single numeric condition AST to simple SELECT" in {
      val ast = StringEqual("string_fact", StringFact("somevalue"), Moment.Now(now))
      val result = SqlStringEquals(2, StringFact("somevalue"), now)
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate 'or' node to SQL UNION" in {
      val ast = OrNode(Seq(NumericEqual("numeric_fact", NumericFact(10.0), Moment.Now(now)),
        StringEqual("string_fact", StringFact("somevalue"), Moment.Now(now))))
      val result = UnionSql(Seq(
        SqlNumericEquals(1, NumericFact(10.0), now),
        SqlStringEquals(2, StringFact("somevalue"), now)
      ))
      queryTranslator(ast, facts) must equalTo(result)
    }

    "translate 'and' node AST to SQL INTERSECTION" in {
      val ast = AndNode(Seq(NumericEqual("numeric_fact", NumericFact(10.0), Moment.Now(now)),
        StringEqual("string_fact", StringFact("somevalue"), Moment.Now(now))))
      val result = IntersectionSql(Seq(
        SqlNumericEquals(1, NumericFact(10.0), now),
        SqlStringEquals(2, StringFact("somevalue"), now)
      ))
      queryTranslator(ast, facts) must equalTo(result)
    }
  }
}
