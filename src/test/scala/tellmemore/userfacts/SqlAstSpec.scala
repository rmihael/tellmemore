package tellmemore.userfacts

import org.specs2.mutable.Specification
import tellmemore.{StringFact, NumericFact}

class SqlAstSpec extends Specification {
  "SQL AST" should {
    "render tree to SQL query" in {
      val ast = IntersectionSql(Seq(
        BasicSql(SqlCondition(1, NumericFact(10.0))),
        BasicSql(SqlCondition(2, StringFact("somevalue")))
      ))
      ast.sql must equalTo("(SELECT DISTINCT users.external_id FROM fact_values JOIN users ON fact_values.user_id = users.id WHERE fact_values.fact_id=1 AND fact_values.numeric_value=10.0)" +
                           " INTERSECT " +
                           "(SELECT DISTINCT users.external_id FROM fact_values JOIN users ON fact_values.user_id = users.id WHERE fact_values.fact_id=2 AND fact_values.string_value='somevalue')")
    }

    "avoid SQL injections" in {
      skipped
    }
  }
}
