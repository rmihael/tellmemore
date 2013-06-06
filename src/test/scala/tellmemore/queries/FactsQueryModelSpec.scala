package tellmemore.queries

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import org.specs2.matcher.DataTables

import tellmemore.{NumericFact, StringFact}

class FactsQueryModelSpec extends Specification with DataTables {
  "JSON parser" should {
    "parse simple string fact condition" in {
      val str = """{"fact": "string"}"""
      FactsQueryModel.parseJson(Json.parse(str)) must beRight(FactsQueryAst.Condition("fact", StringFact("string")))
    }

    "parse simple numeric fact condition" in {
      val str = """{"fact": 2.5}"""
      FactsQueryModel.parseJson(Json.parse(str)) must beRight(FactsQueryAst.Condition("fact", NumericFact(2.5)))
    }

    "parse simple $and operator" in {
      val str = """{"$and": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryModel.parseJson(Json.parse(str)) must beRight(
        FactsQueryAst.AndNode(Seq(
          FactsQueryAst.Condition("fact", NumericFact(2.5)),
          FactsQueryAst.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple $or operator" in {
      val str = """{"$or": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryModel.parseJson(Json.parse(str)) must beRight(
        FactsQueryAst.OrNode(Seq(
          FactsQueryAst.Condition("fact", NumericFact(2.5)),
          FactsQueryAst.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple deeply embedded queries" in {
      val str = """{"$and": [{"$or": [{"fact": 2.5}, {"fact2": "string"}]}, {"$and": [{"fact3": 5.5}, {"fact4": "string2"}]}]}"""
      FactsQueryModel.parseJson(Json.parse(str)) must beRight(
        FactsQueryAst.AndNode(Seq(
          FactsQueryAst.OrNode(Seq(
            FactsQueryAst.Condition("fact", NumericFact(2.5)),
            FactsQueryAst.Condition("fact2", StringFact("string"))
          )),
          FactsQueryAst.AndNode(Seq(
            FactsQueryAst.Condition("fact3", NumericFact(5.5)),
            FactsQueryAst.Condition("fact4", StringFact("string2"))
          ))
        ))
      )
    }
  }
}
