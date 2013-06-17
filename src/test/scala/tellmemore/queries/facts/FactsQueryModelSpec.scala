package tellmemore.queries.facts

import org.specs2.mutable.Specification
import play.api.libs.json.Json

import tellmemore.{FactType, UserFact, NumericFact, StringFact}
import tellmemore.userfacts.UserFactModel
import org.specs2.mock.Mockito
import org.joda.time.DateTime

class FactsQueryModelSpec extends Specification with Mockito {
  "FactsQueryModel" should {
    isolated

    val userFactModel = mock[UserFactModel]
    val factsQueryModel = FactsQueryModel(userFactModel)

    "return None when trying to search by invalid JSON string" in {
      val query = """{"field": "value}"""  // JSON with unclosed quotation
      factsQueryModel.find("some@client.com", query) must beNone
    }

    "return None when trying to search by malformed JSON" in {
      val query = Json.parse("""{"$BAD": [{"fact": 2.5}, {"fact2": "string"}]}""")
      factsQueryModel.find("some@client.com", query) must beNone
    }

    "return None on unknown fact in query" in {
      userFactModel.getUserFactsForClient("some@client.com") returns Map()
      factsQueryModel.find("some@client.com", """{"fact": "string"}""") must beNone
    }

    "return None on non-matching value type for fact" in {
      val fact = UserFact("some@client.com", "fact", FactType.Numeric, DateTime.now)
      userFactModel.getUserFactsForClient("some@client.com") returns Map("fact" -> fact)
      factsQueryModel.find("some@client.com", """{"fact": "string"}""") must beNone
    }

    "delegate execution of correct queries to UserFactModel" in {
      val fact = UserFact("some@client.com", "fact", FactType.String, DateTime.now)
      userFactModel.getUserFactsForClient("some@client.com") returns Map("fact" -> fact)
      userFactModel.find(any[FactsQuery]) returns Set("someuser")
      factsQueryModel.find("some@client.com", """{"fact": "string"}""") must beSome(Set("someuser"))
    }
  }

  "JSON parser" should {
    "give back error for malformed JSON query" in {
      val str = """{"$BAD": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beLeft
    }

    "parse simple string fact condition" in {
      val str = """{"fact": "string"}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(FactsQueryAst.Condition("fact", StringFact("string")))
    }

    "parse simple numeric fact condition" in {
      val str = """{"fact": 2.5}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(FactsQueryAst.Condition("fact", NumericFact(2.5)))
    }

    "parse simple $and operator" in {
      val str = """{"$and": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
        FactsQueryAst.AndNode(Seq(
          FactsQueryAst.Condition("fact", NumericFact(2.5)),
          FactsQueryAst.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple $or operator" in {
      val str = """{"$or": [{"fact": 2.5}, {"fact2": "string"}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
        FactsQueryAst.OrNode(Seq(
          FactsQueryAst.Condition("fact", NumericFact(2.5)),
          FactsQueryAst.Condition("fact2", StringFact("string"))
        ))
      )
    }

    "parse simple deeply embedded queries" in {
      val str = """{"$and": [{"$or": [{"fact": 2.5}, {"fact2": "string"}]}, {"$and": [{"fact3": 5.5}, {"fact4": "string2"}]}]}"""
      FactsQueryAst.parse(Json.parse(str)) must beRight(
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
