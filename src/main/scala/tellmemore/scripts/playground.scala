import org.springframework.context.support.ClassPathXmlApplicationContext
import tellmemore._
import tellmemore.clients._
import tellmemore.users._
import tellmemore.events._
import tellmemore.userfacts._
import org.scala_tools.time.Imports._

object Playground {
  def main(args: Array[String]) {
    val context = new ClassPathXmlApplicationContext(
      "classpath*:/META-INF/spring-config.xml",
      "classpath*:/META-INF/data-sources.xml"
    )
    val clientModel = context.getBean("clientModel").asInstanceOf[ClientModel]
    val userModel = context.getBean("userModel").asInstanceOf[UserModel]
    val eventModel = context.getBean("eventModel").asInstanceOf[EventModel]
    val userFactModel = context.getBean("userFactModel").asInstanceOf[UserFactModel]

    userModel.bulkInsert(Set(User(UserId("someid", "userid"), DateTime.now)))

    clientModel.create(Client("rmihael@gmail.com", "Michael", DateTime.now))
    userModel.bulkInsert(Set(User(UserId("rmihael@gmail.com", "m.korbakov@nimble.com"), DateTime.now)))
    userModel.getAllByClientId("rmihael@gmail.com")
    userFactModel.setForUser(UserId("rmihael@gmail.com", "m.korbakov@nimble.com"),
      Map("string_fact" -> StringFact("String value"), "numeric_fact" -> NumericFact(2.5)))

    userModel.bulkInsert(Set(User(UserId("someid", "userid"), DateTime.now)))
  }
}