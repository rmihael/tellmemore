package tellmemore.users

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.scala.transaction.support.TransactionManagement

case class UserModel(userDao: UserDao, transactionManager: PlatformTransactionManager) extends TransactionManagement {
  def getById(id: UserId): Option[User] = transactional() { txStatus =>
    userDao.getById(id)
  }

  def getAllByClientId(clientId: String): Set[User] = transactional() { txStatus =>
    userDao.getAllByClientId(clientId)
  }

  /**
   * This method creates new users.
   * @note Method expects that parent client of these users is already existing.
   * @note All new users must belong to the same client
   * @param users users to create
   * @return If all users were created successfully then result is Right(<number of created users>). If creating users
   *         failed due to missing client then result would be Left(<client id>) and no changes in the system state
   *         are made.
   */
  def bulkInsert(users: Set[User]): Either[String, Int] = {
    transactional() { txStatus =>
      // TODO: Add transaction rollback in case of Left answer from DAO
      userDao.bulkInsert(users)
    }
  }

  /**
   * This method creates new user
   * @note Method expects that parent client of the user is already existing
   * @param user user to create
   * @return If user was created successfully then return value would be Right(1) (for consistency with bulkInsert). If
   *         creating user failed missing client then return value is Left(<client id>) and no changes in the system
   *         state are made.
   */
  def insert(user: User): Either[String, Int] = {
    bulkInsert(Set(user))
  }
}
