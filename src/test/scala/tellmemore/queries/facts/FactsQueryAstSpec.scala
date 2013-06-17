package tellmemore.queries.facts

import org.specs2.mutable.Specification

import tellmemore.{StringFact, NumericFact}

class FactsQueryAstSpec extends Specification {
  import FactsQueryAst._

  val ast = AndNode(Seq(
              OrNode(Seq(Condition("fact", NumericFact(2.5)), Condition("fact2", StringFact("string")))),
              AndNode(Seq(Condition("fact3", NumericFact(5.5)), Condition("fact4", StringFact("string2"))))
            ))

  "FactsQueryAst" should {
    "check dependencies with dependsOn" in {
      ast.dependsOn("fact") must beTrue
      ast.dependsOn("no-fact") must beFalse
    }
  }
}
