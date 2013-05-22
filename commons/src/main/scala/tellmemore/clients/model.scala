package tellmemore.clients

trait ClientModelComponent {
  val clientModel: ClientModel

  trait ClientModel {
    def getById(id: String): Option[Client]
  }
}

trait ClientModelComponentImpl extends ClientModelComponent{
  this: ClientDaoComponent =>

  class ClientModelImpl extends ClientModel {
    def getById(id: String) = clientDao.getById(id)
  }
}
