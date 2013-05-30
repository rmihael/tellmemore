package tellmemore.clients

import scala.language.postfixOps

import javax.sql.DataSource

import org.scala_tools.time.Imports._
import anorm._
import anorm.SqlParser._

import tellmemore.infrastructure.DB
import tellmemore.Client
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement

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

trait ClientDao {
  def getById(id: String): Option[Client]
  def getAll: Set[Client]
  def create(client: Client)
  def deleteById(id: String)
}

case class PostgreSqlClientDao(dataSource: DataSource) extends ClientDao {
  private[this] val simple =
    get[String]("clients.email") ~
    get[String]("clients.name") ~
    get[Int]("clients.created") map {
          case email~name~created => Client(email, name, new DateTime(created * 1000L))
    }

  def getById(id: String): Option[Client] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT email, name, created FROM clients WHERE email={email}").on("email" -> id).as(simple.singleOpt)
  }

  def getAll: Set[Client] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT email, name, created FROM clients").as(simple *).toSet
  }

  def create(client: Client) {
    DB.withConnection(dataSource) { implicit connection =>
      SQL("INSERT INTO clients(email, name, created) VALUES ({email}, {name}, {created})")
        .on("email" -> client.id,
            "name" -> client.name,
            "created" -> client.created.millis / 1000)
        .executeInsert()
    }
  }

  def deleteById(id: String) {
    DB.withConnection(dataSource) {
      implicit connection =>
        SQL("DELETE FROM clients WHERE email={email}").on("email" -> id).executeUpdate()
    }
  }
}
