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
import java.sql.SQLException

case class UserModel(userDao: UserDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getById(id: UserId): Option[User] = transactional() { txStatus =>
    userDao.getById(id)
  }

  def getAllByClientId(clientId: String): Set[User] = transactional() { txStatus =>
    userDao.getAllByClientId(clientId)
  }

  def bulkInsert(users: Set[User]): Either[String, Int] = {
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
  def bulkInsert(users: Set[User]): Either[String, Int]
}

case class PostgreSqlUserDao(dataSource: DataSource) extends UserDao {
  private[this] val simple =
    get[String]("users.external_id") ~
    get[Int]("users.created") map {
      case externalId~created => (externalId, new DateTime(created * 1000L))
    }

  def getById(id: UserId): Option[User] = DB.withConnection(dataSource) { implicit connection =>
    SQL("""SELECT external_id, created FROM users
           WHERE client_id=(SELECT id FROM clients WHERE email = {client_id}) AND external_id={external_id}""")
      .on("external_id" -> id.externalId, "client_id" -> id.clientId)
      .as(simple.singleOpt) map { case (_, created) => User(id, created) }
  }

  def getAllByClientId(clientId: String): Set[User] = DB.withConnection(dataSource) { implicit connection =>
    (SQL("SELECT external_id, created FROM users WHERE client_id=(SELECT id FROM clients WHERE email={client_id})")
      .on("client_id" -> clientId)
      .as(simple *) map { case (externalId, created) => User(UserId(clientId, externalId), created)})
    .toSet
  }

  def bulkInsert(users: Set[User]): Either[String, Int] = {
    require(Set(users map {_.id.clientId}).size <= 1)
    users.size match {
      case 0 => Right(0)
      case _ => {
        val insertQuery = SQL(
          """INSERT INTO users(client_id, external_id, created)
             VALUES((SELECT id FROM clients WHERE email={client_id}), {external_id}, {created})""")
        val batchInsert = (insertQuery.asBatch /: users) {
          (sql, user) => sql.addBatchParams(user.id.clientId, user.id.externalId, user.created.millis / 1000)
        }
        try {
          DB.withConnection(dataSource) { implicit connection =>
            batchInsert.execute()
            Right(users.size)
          }
        } catch {
          // users.head can't fail because there're at least one item in the set
          case exc: SQLException if exc.getSQLState == SQL_STATE_NULLS_NOT_ALLOWED => Left(users.head.id.clientId)
        }
      }
    }
  }

  private[this] val SQL_STATE_NULLS_NOT_ALLOWED = "23502"
}
