package tellmemore.clients

import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.specification.Scope
import org.springframework.transaction.PlatformTransactionManager
import org.scala_tools.time.Imports._

import tellmemore.Client

class ClientModelSpec extends Specification with Mockito {
  isolated

  val txManager = mock[PlatformTransactionManager]
  val clientDao = mock[ClientDao]
  val clientModel = ClientModel(clientDao, txManager)

  "The client model" should {
    "delegate getAll to ClientDao" in {
      val clients = Set(Client("email1@domain.com", "Test user 2", DateTime.now),
                      Client("email2@domain.com", "Test user 2", DateTime.now))
      clientDao.getAll returns clients

      clientModel.getAll must equalTo(clients)
    }

    "delegate getById to ClientDao" in {
      val client = Client("email1@domain.com", "Test user 1", DateTime.now)
      clientDao.getById(client.id) returns Some(client)

      clientModel.getById(client.id) must beSome(client)
    }

    "delegate create to ClientDao" in {
      val client = Client("email1@domain.com", "Test user 2", DateTime.now)
      clientModel.create(client)

      there was one(clientDao).create(client)
    }

    "delegate deleteById to ClientDao" in {
      val clientId = "email1@domain.com"
      clientModel.deleteById(clientId)

      there was one(clientDao).deleteById(clientId)
    }
  }
}
