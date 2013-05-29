package tellmemore.users

import scala.language.postfixOps

import javax.sql.DataSource

import anorm.SqlParser._
import anorm._
import org.scala_tools.time.Imports._
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement

import tellmemore.{User, UserId}
import tellmemore.infrastructure.DB

case class UserModel(userDao: UserDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getById(id: UserId): Option[User] = transactional() { txStatus =>
    userDao.getById(id)
  }

  def getAllByClientId(clientId: String): Set[User] = transactional() { txStatus =>
    userDao.getAllByClientId(clientId)
  }

  def bulkInsert(users: Set[User]) {
    transactional() { txStatus =>
      userDao.bulkInsert(users)
    }
  }

  def insert(user: User) {
    bulkInsert(Set(user))
  }
}

trait UserDao {
  def getById(id: UserId): Option[User]
  def getAllByClientId(clientId: String): Set[User]
  def bulkInsert(users: Set[User])
}

case class PostgreSqlUserDao(dataSource: DataSource) extends UserDao {
  private[this] val simple =
    get[Long]("users.client_id") ~
    get[String]("users.external_id") ~
    get[Int]("users.created") map {
      case clientId~externalId~created => User(UserId(clientId.toString, externalId), new DateTime(created * 1000L))
    }

  def getById(id: UserId): Option[User] = DB.withConnection(dataSource) { implicit connection =>
    SQL("""SELECT id, client_id, external_id, human_readable_id, created FROM users
           WHERE client_id={client_id} AND external_id={external_id}""")
      .on("external_id" -> id.externalId, "client_id" -> id.clientId)
      .as(simple.singleOpt)
  }

  def getAllByClientId(clientId: String): Set[User] = DB.withConnection(dataSource) { implicit connection =>
    SQL("SELECT id, client_id, external_id, created FROM users WHERE client_id={client_id}")
      .on("client_id" -> clientId)
      .as(simple *)
      .toSet
  }

  def bulkInsert(users: Set[User]) {
    DB.withConnection(dataSource) { implicit connection =>
      val insertQuery = SQL(
        """INSERT INTO users(client_id, external_id, created)
           VALUES({client_id}, {external_id}, {human_readable_id}, {created})""")
      val batchInsert = (insertQuery.asBatch /: users) {
        (sql, user) => sql.addBatchParams(user.id.clientId, user.id.externalId, user.created.millis / 1000)
      }
      batchInsert.execute()
    }
  }
}
