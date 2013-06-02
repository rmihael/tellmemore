package tellmemore.clients

import tellmemore.Client

trait ClientDao {
  def getById(id: String): Option[Client]
  def getAll: Set[Client]
  def create(client: Client)
  def deleteById(id: String)
}
