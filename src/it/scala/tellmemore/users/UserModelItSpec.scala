package tellmemore.users

import org.springframework.beans.factory.annotation.Autowired

import tellmemore.{Client, IntegrationTest}
import org.scala_tools.time.Imports._
import tellmemore.clients.ClientModel

class UserModelItSpec extends IntegrationTest {
  @Autowired var userModel: UserModel = _
  @Autowired var clientModel: ClientModel = _

  val client = Client("email1@domain.com", "Test user 1", DateTime.now)
  val user = User(UserId(client.id, "newuser"), "New user", DateTime.now)

  "UserModel" should {
    "give None for absent users" in {
      userModel.getById(UserId("client@domain.com", "absent_id")) must beNone
    }

    "persist user for existing client" in {
      clientModel.create(client)
      userModel.insert(user)
      userModel.getById(UserId(client.id, "newuser")) must beSome(user)
    }

    "reject to persist user for non-existing client" in {
      userModel.insert(user) must beLeft(client.id)
    }

    "successful bulkInsert must give back number of created users" in {
      clientModel.create(client)
      val users = List.tabulate(5) {n => User(UserId(client.id, s"newuser$n"), s"New user $n", DateTime.now)}
      userModel.bulkInsert(users.toSet) must beRight(users.size)
    }

    "getAllByClientId must give back all users for client" in {
      clientModel.create(client)
      userModel.bulkInsert(List.tabulate(5) {n => User(UserId(client.id, s"newuser$n"), s"New user $n", DateTime.now)} toSet)
      userModel.bulkInsert(List.tabulate(5) {n => User(UserId(client.id, s"newuser${n+5}"), s"New user $n", DateTime.now)} toSet)
      userModel.getAllByClientId(client.id) must haveLength(10)
    }

    "must not create user with duplicated id" in {
      clientModel.create(client)
      userModel.insert(user)
      userModel.insert(User(user.id, "Some user", DateTime.now))
      skipped
      userModel.getAllByClientId(client.id) must haveLength(1)
    }

    "bulkInsert of empty set must not fail" in {
      userModel.bulkInsert(Set()) must beRight(0)
    }
  }
}
