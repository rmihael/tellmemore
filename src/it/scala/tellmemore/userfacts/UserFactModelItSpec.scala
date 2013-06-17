package tellmemore.userfacts

import org.springframework.beans.factory.annotation.Autowired
import org.scala_tools.time.Imports._
import org.specs2.specification.Scope

import tellmemore.clients.ClientModel
import tellmemore.users.UserModel
import tellmemore.queries.facts.FactsQueryAst.{AndNode, OrNode, Condition}
import tellmemore.{StringFact, NumericFact}
import tellmemore.{UserId, User, Client, IntegrationTest}
import tellmemore.queries.facts.FactsQuery

class UserFactModelItSpec extends IntegrationTest {
  @Autowired var userModel: UserModel = _
  @Autowired var clientModel: ClientModel = _
  @Autowired var userFactModel: UserFactModel = _

  trait clientsAndUsers extends Scope {
    val client = Client("email1@domain.com", "Test user 1", DateTime.now)
    val user = User(UserId(client.id, "newuser"), DateTime.now)
    clientModel.create(client)
    userModel.insert(user)
  }

  "UserFactModel" should {
    "persist user fact values" in {
      skipped
    }

    "create non-existing facts for client on setForUser" in {
      skipped
    }

    "detect inconsistencies in between fact type and value in setForUser" in {
      skipped
    }

    "rollback any changes to facts in case if any error happened during inserting values in setForUser" in {
      skipped
    }

    "list all client's facts with getUserFactsForClient" in {
      skipped
    }

    "find users by single condition query" in new clientsAndUsers {
      val ast = Condition("fact", NumericFact(2.5))
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5)))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by OR query" in new clientsAndUsers {
      val ast = OrNode(Seq(Condition("fact", NumericFact(2.5)), Condition("fact2", StringFact("string"))))
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("notstring")))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by AND query" in new clientsAndUsers {
      val ast = AndNode(Seq(Condition("fact", NumericFact(2.5)), Condition("fact2", StringFact("string"))))
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("string")))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by complex AND-OR query" in new clientsAndUsers {
      val ast = AndNode(Seq(
        OrNode(Seq(Condition("fact", NumericFact(2.5)), Condition("fact2", StringFact("string")))),
        AndNode(Seq(Condition("fact3", NumericFact(5.5)), Condition("fact4", StringFact("string2"))))
      ))
      val query = FactsQuery(client.id, ast)
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("notstring"),
                                            "fact3" -> NumericFact(5.5), "fact4" -> StringFact("string2")))
      userFactModel.find(query) must equalTo(Set(user.id.externalId))
    }
  }
}
