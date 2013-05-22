package tellmemore.clients

import anorm._
import anorm.SqlParser._
import tellmemore.SqlDbProviderComponent

trait ClientDaoComponent {
  val clientDao: ClientDao

  trait ClientDao {
    def getById(id: String): Option[Client]
  }
}

trait SqlClientDaoComponent extends ClientDaoComponent {
  this: SqlDbProviderComponent =>

  private[this] val simple = get[Pk[Long]]("clients.id") ~ get[String]("clients.name") map {
    case id~name => Client(id.toString, name)
  }

  class ClientDaoImpl extends ClientDao {
    def getById(id: String) = {
      DB.withConnection {implicit connection =>
        SQL("SELECT id, name, FROM clients WHERE id = {id}").on('id -> id).as(simple.singleOpt)
      }
    }
  }
}
