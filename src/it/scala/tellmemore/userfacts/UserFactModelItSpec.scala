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
import tellmemore.queries.Moment

class UserFactModelItSpec extends IntegrationTest {
  @Autowired var userModel: UserModel = _
  @Autowired var clientModel: ClientModel = _
  @Autowired var userFactModel: UserFactModel = _

  trait clientsAndUsers extends Scope {
    val client = Client("email1@domain.com", "Test user 1", DateTime.now)
    val user = User(UserId(client.id, "newuser"), DateTime.now)
    val user2 = User(UserId(client.id, "newuser2"), DateTime.now)
    clientModel.create(client)
    userModel.insert(user)
    userModel.insert(user2)
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
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5)))
      val ast = Condition("fact", NumericFact(2.5), Moment.Now(DateTime.now))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by OR query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("notstring")))
      val now = DateTime.now
      val ast = OrNode(Seq(Condition("fact", NumericFact(2.5), Moment.Now(now)),
                           Condition("fact2", StringFact("string"), Moment.Now(now))))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by AND query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("string")))
      val now = DateTime.now
      val ast = AndNode(Seq(Condition("fact", NumericFact(2.5), Moment.Now(now)),
                            Condition("fact2", StringFact("string"), Moment.Now(now))))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by complex AND-OR query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> NumericFact(2.5), "fact2" -> StringFact("notstring"),
                                            "fact3" -> NumericFact(5.5), "fact4" -> StringFact("string2")))
      val now = DateTime.now
      val ast = AndNode(Seq(
        OrNode(Seq(Condition("fact", NumericFact(2.5), Moment.Now(now)),
                   Condition("fact2", StringFact("string"), Moment.Now(now)))),
        AndNode(Seq(Condition("fact3", NumericFact(5.5), Moment.Now(now)),
                    Condition("fact4", StringFact("string2"), Moment.Now(now))))
      ))
      val query = FactsQuery(client.id, ast)
      userFactModel.find(query) must equalTo(Set(user.id.externalId))
    }

    "find users by condition with time specified" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact2" -> StringFact("a")))
      userFactModel.setForUser(user2.id, Map("fact2" -> StringFact("b")))
      userFactModel.setForUser(user.id, Map("fact2" -> StringFact("b")))
      userFactModel.setForUser(user2.id, Map("fact2" -> StringFact("c")))
      val now = DateTime.now
      userFactModel.setForUser(user.id, Map("fact2" -> StringFact("c")))
      userFactModel.setForUser(user2.id, Map("fact2" -> StringFact("d")))

      val ast = Condition("fact2", StringFact("b"), Moment.Timestamp(now))
      val query = FactsQuery(client.id, ast)
      userFactModel.find(query) must equalTo(Set(user.id.externalId))
    }
  }
}
