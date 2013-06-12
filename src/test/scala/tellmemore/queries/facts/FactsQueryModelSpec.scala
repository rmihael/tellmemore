package tellmemore.queries.facts

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import org.specs2.matcher.DataTables

import tellmemore.{NumericFact, StringFact}

class FactsQueryModelSpec extends Specification with DataTables {
  "JSON parser" should {
    "parse simple string fact condition" in {
      val str = """{"fact": "string"}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(FactsQueryAstT.Condition("fact", StringFact("string")))
    }

    "parse simple numeric fact condition" in {
      val str = """{"fact": 2.5}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(FactsQueryAstT.Condition("fact", NumericFact(2.5)))
    }

    "parse simple $and operator" in {
      val str = """{"$and": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
        FactsQueryAstT.AndNode(Seq(
          FactsQueryAstT.Condition("fact", NumericFact(2.5)),
          FactsQueryAstT.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple $or operator" in {
      val str = """{"$or": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
        FactsQueryAstT.OrNode(Seq(
          FactsQueryAstT.Condition("fact", NumericFact(2.5)),
          FactsQueryAstT.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple deeply embedded queries" in {
      val str = """{"$and": [{"$or": [{"fact": 2.5}, {"fact2": "string"}]}, {"$and": [{"fact3": 5.5}, {"fact4": "string2"}]}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
        FactsQueryAstT.AndNode(Seq(
          FactsQueryAstT.OrNode(Seq(
            FactsQueryAstT.Condition("fact", NumericFact(2.5)),
            FactsQueryAstT.Condition("fact2", StringFact("string"))
          )),
          FactsQueryAstT.AndNode(Seq(
            FactsQueryAstT.Condition("fact3", NumericFact(5.5)),
            FactsQueryAstT.Condition("fact4", StringFact("string2"))
          ))
        ))
      )
    }
  }
}
