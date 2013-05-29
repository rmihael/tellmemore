package tellmemore.clients

import scala.language.postfixOps

import javax.sql.DataSource

import org.scala_tools.time.Imports._
import anorm._
import anorm.SqlParser._

import tellmemore.infrastructure.DB
import tellmemore.{ClientRegistrationData, Client}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement

case class ClientModel(clientDao: ClientDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getById(id: String): Option[Client] = transactional() { txStatus =>
    clientDao.getById(id)
  }

  def getAll: Set[Client] = transactional() { txStatus =>
    clientDao.getAll
  }

  def create(clientData: ClientRegistrationData): Option[Client] = transactional() { txStatus =>
    clientDao.create(clientData)
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
  def create(clientData: ClientRegistrationData): Option[Client]
  def deleteById(id: String)
}

case class PostgreSqlClientDao(dataSource: DataSource) extends ClientDao {
  private[this] val simple =
    get[Pk[Long]]("clients.id") ~
    get[String]("clients.name") ~
    get[Int]("clients.created") ~
    get[Int]("clients.last_login") map {
          case id~name~created~lastLogin => Client(id.toString, name, new DateTime(created * 1000L))
    }

  def getById(id: String): Option[Client] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT id, name, created, last_login FROM clients WHERE id={id}").on("id" -> id).as(simple.singleOpt)
  }

  def getAll: Set[Client] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT id, name, created, last_login FROM clients").as(simple *).toSet
  }

  def create(clientData: ClientRegistrationData): Option[Client] = {
    val id: Option[Long] = DB.withConnection(dataSource) { implicit connection =>
      SQL("INSERT INTO clients(name, created, last_login) VALUES ({name}, {created}, {last_login})")
        .on("name" -> clientData.name,
            "created" -> clientData.created.millis / 1000)
        .executeInsert()
    }
    id map {someId => Client(someId.toString, clientData.name, clientData.created)}
  }

  def deleteById(id: String) {
    DB.withConnection(dataSource) {
      implicit connection =>
        SQL("DELETE FROM clients WHERE id={id").on("id" -> id).executeUpdate()
    }
  }
}
