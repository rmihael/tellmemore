package tellmemore.users

trait UserDao {
  def getById(id: UserId): Option[User]
  def getAllByClientId(clientId: String): Set[User]
  def bulkInsert(users: Set[User]): Either[String, Int]
}
