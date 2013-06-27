package tellmemore.userfacts

import org.springframework.beans.factory.annotation.Autowired
import org.scala_tools.time.Imports._
import org.specs2.specification.Scope

import tellmemore.queries.facts.FactsQueryAst._
import tellmemore.IntegrationTest
import tellmemore.queries.Moment
import tellmemore.users.{UserModel, User, UserId}
import tellmemore.Client
import tellmemore.clients.ClientModel
import tellmemore.queries.facts.FactsQuery

class UserFactModelItSpec extends IntegrationTest {
  @Autowired var userModel: UserModel = _
  @Autowired var clientModel: ClientModel = _
  @Autowired var userFactModel: UserFactModel = _

  trait clientsAndUsers extends Scope {
    val client = Client("email1@domain.com", "Test user 1", DateTime.now)
    val user = User(UserId(client.id, "newuser"), "New user", DateTime.now)
    val user2 = User(UserId(client.id, "newuser2"), "Other new user", DateTime.now)
    clientModel.create(client)
    userModel.insert(user)
    userModel.insert(user2)
  }

  "UserFactModel" should {
    "create non-existing facts for client on setForUser" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact1" -> FactValue("value"),
                                            "fact2" -> FactValue(3.5)))
      val facts = userFactModel.getUserFactsForClient(user.id.clientId)
      (facts("fact1"), facts("fact2")) match {
        case (UserFact.StringFact(client.id, "fact1", _), UserFact.NumericFact(client.id, "fact2", _)) => success
        case f => failure(f.toString())
      }
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

    "find users by single numeric equal" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5)))
      val ast = NumericEqual("fact", 2.5, Moment.Now(DateTime.now))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by single numeric greater then" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5)))
      val ast = NumericGreaterThen("fact", 2.0, Moment.Now(DateTime.now))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by single numeric less then" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5)))
      val ast = NumericLessThen("fact", 3.0, Moment.Now(DateTime.now))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by single string equal" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact2" -> FactValue("value")))
      val ast = StringEqual("fact2", "value", Moment.Now(DateTime.now))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }


    "find users by OR query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5), "fact2" -> FactValue("notstring")))
      val now = DateTime.now
      val ast = OrNode(Seq(NumericEqual("fact", 2.5, Moment.Now(now)),
                           StringEqual("fact2", "string", Moment.Now(now))))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by AND query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5), "fact2" -> FactValue("string")))
      val now = DateTime.now
      val ast = AndNode(Seq(NumericEqual("fact", 2.5, Moment.Now(now)),
                            StringEqual("fact2", "string", Moment.Now(now))))
      userFactModel.find(FactsQuery(client.id, ast)) must equalTo(Set(user.id.externalId))
    }

    "find users by complex AND-OR query" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact" -> FactValue(2.5), "fact2" -> FactValue("notstring"),
                                            "fact3" -> FactValue(5.5), "fact4" -> FactValue("string2")))
      val now = DateTime.now
      val ast = AndNode(Seq(
        OrNode(Seq(NumericEqual("fact", 2.5, Moment.Now(now)),
                   StringEqual("fact2", "string", Moment.Now(now)))),
        AndNode(Seq(NumericEqual("fact3", 5.5, Moment.Now(now)),
                    StringEqual("fact4", "string2", Moment.Now(now))))
      ))
      val query = FactsQuery(client.id, ast)
      userFactModel.find(query) must equalTo(Set(user.id.externalId))
    }

    "find users by condition with time specified" in new clientsAndUsers {
      userFactModel.setForUser(user.id, Map("fact2" -> FactValue("a")))
      userFactModel.setForUser(user2.id, Map("fact2" -> FactValue("b")))
      userFactModel.setForUser(user.id, Map("fact2" -> FactValue("b")))
      userFactModel.setForUser(user2.id, Map("fact2" -> FactValue("c")))
      val now = DateTime.now
      userFactModel.setForUser(user.id, Map("fact2" -> FactValue("c")))
      userFactModel.setForUser(user2.id, Map("fact2" -> FactValue("d")))

      val ast = StringEqual("fact2", "b", Moment.Timestamp(now))
      val query = FactsQuery(client.id, ast)
      userFactModel.find(query) must equalTo(Set(user.id.externalId))
    }

    "avoid SQL injections in facts queries" in {
      skipped
    }
  }
}
