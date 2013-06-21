
import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.GlobalSettings
import controllers.MetadataService
import tellmemore.events.EventModel
import tellmemore.userfacts.UserFactModel

/**
 * Global object that defines creation of scala routes
 */
object Global extends GlobalSettings {
  val context = new ClassPathXmlApplicationContext(
    "classpath*:/META-INF/spring-config.xml",
    "classpath*:/META-INF/data-sources.xml"
  )
  override def getControllerInstance[A](controllerClass: Class[A]): A = {
      if (controllerClass == classOf[MetadataService]) {
        val eventModel = context.getBean("eventModel").asInstanceOf[EventModel]
        val factModel = context.getBean("userFactModel").asInstanceOf[UserFactModel]
        MetadataService(eventModel, factModel).asInstanceOf[A]
      } else {
        super.getControllerInstance(controllerClass)
      }
  }

}
