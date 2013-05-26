package tellmemore.clients

case class ClientModel(clientDao: ClientDao) {
  def getById(id: String): Either[Exception, Option[Client]] = clientDao.getById(id)
}
