package tellmemore.queries.facts

import org.specs2.mutable.Specification

import tellmemore.{StringFact, NumericFact}

class FactsQueryAstSpec extends Specification {
  val ast = FactsQueryAstT.AndNode(Seq(
              FactsQueryAstT.OrNode(Seq(
                FactsQueryAstT.Condition("fact", NumericFact(2.5)),
                FactsQueryAstT.Condition("fact2", StringFact("string"))
              )),
              FactsQueryAstT.AndNode(Seq(
                FactsQueryAstT.Condition("fact3", NumericFact(5.5)),
                FactsQueryAstT.Condition("fact4", StringFact("string2"))
              ))
            ))

  "FactsQueryAst" should {
    "check dependencies with dependsOn" in {
      ast.dependsOn("fact") must beTrue
      ast.dependsOn("no-fact") must beFalse
    }
  }
}
