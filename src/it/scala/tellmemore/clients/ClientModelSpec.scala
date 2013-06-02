package tellmemore.clients

import org.springframework.beans.factory.annotation.Autowired
import org.scala_tools.time.Imports._

import tellmemore.{Client, IntegrationTest}

class ClientModelSpec extends IntegrationTest {
  @Autowired var clientModel: ClientModel = _

  val clients = Set(
    Client("email1@domain.com", "Test user 1", DateTime.now),
    Client("email2@domain.com", "Test user 2", DateTime.now),
    Client("email3@domain.com", "Test user 3", DateTime.now)
  )

  "ClientModel" should {
    "persist clients with 'create'" in {
      clientModel.create(clients.head)
      clientModel.getById("email1@domain.com") must beSome(clients.head)
    }

    "give None for absent clients" in {
      clientModel.getById("missing@domain.com") must beNone
    }

    "list all persisted clients with 'getAll'" in {
      clients.foreach {clientModel.create(_)}
      clientModel.getAll must equalTo(clients)
    }

    "delete persisted user" in {
      clients.foreach {clientModel.create(_)}
      clientModel.deleteById(clients.head.id)
      clientModel.getById(clients.head.id) must beNone
    }
  }
}
