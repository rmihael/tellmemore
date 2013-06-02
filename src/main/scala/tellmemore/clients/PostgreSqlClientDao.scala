package tellmemore.clients

import scala.language.postfixOps

import javax.sql.DataSource

import org.scala_tools.time.Imports._
import anorm._
import anorm.SqlParser._

import tellmemore.infrastructure.DB
import tellmemore.Client

case class PostgreSqlClientDao(dataSource: DataSource) extends ClientDao {
  private[this] val simple =
    get[String]("clients.email") ~
    get[String]("clients.name") ~
    get[Long]("clients.created") map {
          case email~name~created => Client(email, name, new DateTime(created))
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
            "created" -> client.created.millis)
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
