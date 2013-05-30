package tellmemore.users

import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.springframework.transaction.PlatformTransactionManager
import org.scala_tools.time.Imports._
import tellmemore.{UserId, User}

class UserModelSpec extends Specification with Mockito {
  isolated

  val txManager = mock[PlatformTransactionManager]
  val userDao = mock[UserDao]
  val userModel = UserModel(userDao, txManager)

  val user = User(UserId("client_id", "user_id"), DateTime.now)

  "The user model" should {
    "delegate getById to UserDao" in {
      userDao.getById(user.id) returns Some(user)
      userModel.getById(user.id) must beSome(user)
    }

    "delegate getAllByClientId to UserDao" in {
      userDao.getAllByClientId(user.id.clientId) returns Set(user)
      userModel.getAllByClientId(user.id.clientId) must equalTo(Set(user))
    }

    "delegate bulkInsert to UserDao" in {
      userModel.bulkInsert(Set(user))
      there was one(userDao).bulkInsert(Set(user))
    }

    "delegate insert to UserDao" in {
      userModel.insert(user)
      there was one(userDao).bulkInsert(Set(user))
    }
  }
}
