package tellmemore.queries.facts

import org.specs2.mutable.Specification
import play.api.libs.json.Json

import org.specs2.mock.Mockito
import org.joda.time.DateTime
import tellmemore.queries.Moment
import tellmemore.userfacts.{UserFactModel, UserFact}
import tellmemore.stubs.FixedTimeProvider

class FactsQueryModelSpec extends Specification with Mockito {
  import UserFact._

  "FactsQueryModel" should {
    isolated

    val userFactModel = mock[UserFactModel]
    val factsQueryModel = FactsQueryModel(userFactModel, FixedTimeProvider())

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
      val fact = NumericFact("some@client.com", "fact", DateTime.now)
      userFactModel.getUserFactsForClient("some@client.com") returns Map("fact" -> fact)
      factsQueryModel.find("some@client.com", """{"fact": "string"}""") must beNone
    }

    "delegate execution of correct queries to UserFactModel" in {
      val fact = StringFact("some@client.com", "fact", DateTime.now)
      userFactModel.getUserFactsForClient("some@client.com") returns Map("fact" -> fact)
      userFactModel.find(any[FactsQuery]) returns Set("someuser")
      factsQueryModel.find("some@client.com", """{"fact": "string"}""") must beSome(Set("someuser"))
    }
  }

  "JSON parser" should {
    val now = DateTime.now
    val parse = FactsQueryParser(FixedTimeProvider(now))

    "give back error for malformed JSON query" in {
      val str = """{"$BAD": [{"fact": 2.5}, {"fact2": "string"}]}"""
      parse(Json.parse(str)) must beLeft
    }

    "parse simple string fact condition" in {
      val str = """{"fact": "string"}"""
      parse(Json.parse(str)) must beRight(FactsQueryAst.StringEqual("fact", "string", Moment.Now(now)))
    }

    "parse simple numeric fact condition" in {
      val str = """{"fact": 2.5}"""
      parse(Json.parse(str)) must beRight(FactsQueryAst.NumericEqual("fact", 2.5, Moment.Now(now)))
    }

    "parse simple $and operator" in {
      val str = """{"$and": [{"fact": 2.5}, {"fact2": "string"}]}"""
      parse(Json.parse(str)) must beRight(
        FactsQueryAst.AndNode(Seq(
          FactsQueryAst.NumericEqual("fact", 2.5, Moment.Now(now)),
          FactsQueryAst.StringEqual("fact2", "string", Moment.Now(now))
        ))
      )
    }

    "parse simple $or operator" in {
      val str = """{"$or": [{"fact": 2.5}, {"fact2": "string"}]}"""
      parse(Json.parse(str)) must beRight(
        FactsQueryAst.OrNode(Seq(
          FactsQueryAst.NumericEqual("fact", 2.5, Moment.Now(now)),
          FactsQueryAst.StringEqual("fact2", "string", Moment.Now(now))
        ))
      )
    }

    "parse simple deeply embedded queries" in {
      val str = """{"$and": [{"$or": [{"fact": 2.5}, {"fact2": "string"}]}, {"$and": [{"fact3": 5.5}, {"fact4": "string2"}]}]}"""
      parse(Json.parse(str)) must beRight(
        FactsQueryAst.AndNode(Seq(
          FactsQueryAst.OrNode(Seq(
            FactsQueryAst.NumericEqual("fact", 2.5, Moment.Now(now)),
            FactsQueryAst.StringEqual("fact2", "string", Moment.Now(now))
          )),
          FactsQueryAst.AndNode(Seq(
            FactsQueryAst.NumericEqual("fact3", 5.5, Moment.Now(now)),
            FactsQueryAst.StringEqual("fact4", "string2", Moment.Now(now))
          ))
        ))
      )
    }
  }
}
