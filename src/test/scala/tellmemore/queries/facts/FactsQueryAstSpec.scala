package tellmemore.queries.facts

import org.specs2.mutable.Specification

import tellmemore.{StringFact, NumericFact}
import tellmemore.queries.Moment
import org.joda.time.DateTime

class FactsQueryAstSpec extends Specification {
  import FactsQueryAst._

  val ast = AndNode(Seq(
              OrNode(Seq(Condition("fact", NumericFact(2.5), Moment.Now(DateTime.now)),
                         Condition("fact2", StringFact("string"), Moment.Now(DateTime.now)))),
              AndNode(Seq(Condition("fact3", NumericFact(5.5), Moment.Now(DateTime.now)),
                          Condition("fact4", StringFact("string2"), Moment.Now(DateTime.now))))
            ))

  "FactsQueryAst" should {
    "check dependencies with dependsOn" in {
      ast.dependsOn("fact") must beTrue
      ast.dependsOn("no-fact") must beFalse
    }
  }
}
