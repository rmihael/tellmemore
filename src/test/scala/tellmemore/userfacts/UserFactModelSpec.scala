package tellmemore.userfacts

import org.specs2.mutable._
import org.springframework.transaction.PlatformTransactionManager
import org.scala_tools.time.Imports._

import tellmemore.{UserFact, UserId, FactType, NumericFact, StringFact}
import tellmemore.stubs.FixedTimeProvider
import org.specs2.mock.Mockito

class UserFactModelSpec extends Specification with Mockito {
  isolated

  val txManager = mock[PlatformTransactionManager]
  val userFactDao = mock[UserFactDao]
  val now = DateTime.now
  val userFactModel = UserFactModel(userFactDao, txManager, FixedTimeProvider(now))

  val facts = Set(
    UserFact("client@domain.com", "numeric fact", FactType.Numeric, now),
    UserFact("client@domain.com", "string fact", FactType.String, now)
  )
  val userId = UserId("client@domain.com", "user@domain.com")

  "UserFactModel" should {
    "delegate getUserFactsForClient to DAO" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      userFactModel.getUserFactsForClient("client@domain.com") must equalTo(facts map {fact => fact.name -> fact} toMap)
    }

    "create missing facts when doing setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns Set()
      val values = Map("string fact" -> StringFact("string value"), "numeric fact" -> NumericFact(1.0))

      userFactModel.setForUser(userId, values)

      there was one(userFactDao).bulkInsert(Set(
        UserFact(userId.clientId, "string fact", FactType.String, now),
        UserFact(userId.clientId, "numeric fact", FactType.Numeric, now)
      ))
    }

    "detect wrong types for fact values in setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      val values = Map("numeric fact" -> StringFact("string value"), "string fact" -> NumericFact(1.0))

      userFactModel.setForUser(userId, values) must beLeft(values)
    }

    "handle combination of existing and non-existing facts in setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      val values = Map("extra fact" -> StringFact("string value"), "numeric fact" -> NumericFact(1.0))

      userFactModel.setForUser(userId, values) must beRight(values.size)
    }

    "refuse to create any duplicate facts for client" in {
      skipped
    }

    "do not create new facts on bulk insert if any of facts is invalid" in {
      skipped
    }

  }
}
