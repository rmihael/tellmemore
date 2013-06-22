package tellmemore.userfacts

import org.specs2.mutable._
import org.springframework.transaction.PlatformTransactionManager
import org.scala_tools.time.Imports._

import tellmemore.users.UserId
import org.specs2.mock.Mockito
import tellmemore.stubs.FixedTimeProvider

class UserFactModelSpec extends Specification with Mockito {
  import UserFact._

  isolated

  val txManager = mock[PlatformTransactionManager]
  val userFactDao = mock[UserFactDao]
  val now = DateTime.now
  val userFactModel = UserFactModel(userFactDao, txManager, FixedTimeProvider(now))

  val facts: Set[UserFact] = Set(
    NumericFact("client@domain.com", "numeric fact", now),
    StringFact("client@domain.com", "string fact", now)
  )
  val userId = UserId("client@domain.com", "user@domain.com")

  "UserFactModel" should {
    "delegate getUserFactsForClient to DAO" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      userFactModel.getUserFactsForClient("client@domain.com") must equalTo(facts map {fact => fact.name -> fact} toMap)
    }

    "create missing facts when doing setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns Set()
      val values = Map("string fact" -> FactValue.StringValue("string value"), "numeric fact" -> FactValue.NumericValue(1.0))

      userFactModel.setForUser(userId, values)

      there was one(userFactDao).bulkInsert(Set(
        StringFact(userId.clientId, "string fact", now),
        NumericFact(userId.clientId, "numeric fact", now)
      ))
    }

    "detect wrong types for fact values in setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      val values = Map("numeric fact" -> FactValue.StringValue("string value"), "string fact" -> FactValue.NumericValue(1.0))

      userFactModel.setForUser(userId, values) must beLeft(values)
    }

    "handle combination of existing and non-existing facts in setForUser" in {
      userFactDao.getByClientId("client@domain.com") returns facts
      val values = Map("extra fact" -> FactValue.StringValue("string value"), "numeric fact" -> FactValue.NumericValue(1.0))

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
