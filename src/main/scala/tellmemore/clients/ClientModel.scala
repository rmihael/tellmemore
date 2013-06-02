package tellmemore.clients

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement
import tellmemore.Client

@Component
case class ClientModel(clientDao: ClientDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getById(id: String): Option[Client] = transactional() { txStatus =>
    clientDao.getById(id)
  }

  def getAll: Set[Client] = transactional() { txStatus =>
    clientDao.getAll
  }

  def create(client: Client) = transactional() { txStatus =>
    clientDao.create(client)
  }

  def deleteById(id: String) {
    transactional() {
      txStatus =>
        clientDao.deleteById(id)
    }
  }
}
