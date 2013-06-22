package tellmemore.events

import org.specs2.mutable._
import org.specs2.mock.Mockito
import org.springframework.transaction.PlatformTransactionManager

class EventsModelSpec extends Specification with Mockito {
  isolated

  val eventNames = Set[String]("eventName1", "eventName2")
  val eventDao = mock[EventDao]
  val txManager = mock[PlatformTransactionManager]
  val eventModel = EventModel(eventDao, txManager)

  "Events Model" should {
    "delegate getByUserIdAndTimestamp to DAO" in {
      skipped
    }

    "delegate getAllByClientId to DAO" in {
      skipped
    }

    "delegate bulkInsert to DAO" in {
      skipped
    }

    "delegate getEventNames to DAO" in {
      eventDao.getEventNames("client@domain.com") returns eventNames

      eventModel.getEventNames("client@domain.com") must equalTo(eventNames)
    }
  }

}
