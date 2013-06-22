package tellmemore.queries.facts

import org.specs2.mutable.Specification
import org.joda.time.DateTime

import tellmemore.queries.Moment

class FactsQueryAstSpec extends Specification {
  import FactsQueryAst._

  val ast = AndNode(Seq(
              OrNode(Seq(NumericEqual("fact", 2.5, Moment.Now(DateTime.now)),
                StringEqual("fact2", "string", Moment.Now(DateTime.now)))),
              AndNode(Seq(NumericEqual("fact3", 5.5, Moment.Now(DateTime.now)),
                StringEqual("fact4", "string2", Moment.Now(DateTime.now))))
            ))

  "FactsQueryAst" should {
    "check dependencies with dependsOn" in {
      ast.dependsOn("fact") must beTrue
      ast.dependsOn("no-fact") must beFalse
    }
  }
}
