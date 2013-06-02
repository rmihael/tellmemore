package tellmemore

import org.specs2.spring.Specification
import org.springframework.test.context.ContextConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.scala_tools.time.Imports._

import tellmemore.clients.ClientModel

@Transactional
@ContextConfiguration(Array("classpath*:/META-INF/it-data-sources.xml", "classpath*:/META-INF/spring-config.xml"))
class ClientModelSpec extends Specification {
  @Autowired var clientModel: ClientModel = _

  "ClientModel" should {
    "persist clients with 'create'" in {
      val client = Client("email1@domain.com", "Test user", DateTime.now)
      clientModel.create(client)
      clientModel.getById("email1@domain.com") must beSome(client)
    }

    "list all peristed clients with 'getAll'" in {
      val clients = Set(
        Client("email1@domain.com", "Test user 1", DateTime.now),
        Client("email2@domain.com", "Test user 2", DateTime.now),
        Client("email3@domain.com", "Test user 3", DateTime.now)
      )
      clients.foreach {clientModel.create(_)}

      clientModel.getAll must equalTo(clients)
    }
  }
}
