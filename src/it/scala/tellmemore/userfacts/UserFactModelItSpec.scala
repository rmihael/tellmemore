package tellmemore.userfacts

import org.springframework.beans.factory.annotation.Autowired
import org.scala_tools.time.Imports._

import tellmemore.clients.ClientModel
import tellmemore.users.UserModel
import tellmemore.{UserId, User, Client, IntegrationTest}

class UserFactModelItSpec extends IntegrationTest {
  @Autowired var userModel: UserModel = _
  @Autowired var clientModel: ClientModel = _

  val client = Client("email1@domain.com", "Test user 1", DateTime.now)
  val user = User(UserId(client.id, "newuser"), DateTime.now)

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
  }
}
