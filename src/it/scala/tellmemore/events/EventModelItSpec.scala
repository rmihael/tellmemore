package tellmemore.events

import tellmemore.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.specs2.specification.Scope
import tellmemore.scripts.DataGenerator
import tellmemore.scripts.FakeDataGenerator.generateEvents

class EventModelItSpec extends IntegrationTest {
  @Autowired var eventModel: EventModel = _
  @Autowired var fakeDataGenerator: DataGenerator = _

  val eventsMap = Map[String, Int]("event1" -> 10, "event2" -> 15)
  val clientId = "some@client.com"

  trait eventsData extends Scope {
    generateEvents(fakeDataGenerator, clientId, eventsMap)
  }

  "EventModel" should {
    "get all user events by id and specified timestamp" in  {
      skipped
    }

    "get all events by client id" in new eventsData {
      var eventsCount = 0
      eventsMap.values.foreach( eventsCount += _ )

      eventModel.getAllByClientId(clientId).size must equalTo(eventsCount)
    }

    "persist events" in {
      skipped
    }

    "give all unique event names with getEventNames" in new eventsData {
      eventModel.getEventNames(clientId) must equalTo(eventsMap.keySet)
    }

  }

}
