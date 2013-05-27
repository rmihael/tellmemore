import org.apache.commons.dbcp.BasicDataSource
import org.springframework.scala.context.function.FunctionalConfiguration
import com.googlecode.flyway.core.Flyway

import tellmemore.clients.{PostgreSqlClientDao, ClientModel}
import tellmemore.events.{EventModel, PostgreSqlEventDao}
import tellmemore.users.{UserModel, PostgreSqlUserDao}

class ContextConfiguration extends FunctionalConfiguration {
  val dataSource = bean("dataSource") {
    val dataSource = new BasicDataSource()
    dataSource.setDriverClassName("org.h2.Driver")
    dataSource.setUrl("jdbc:h2:mem:tellmemore;MODE=POSTGRESQL")
    dataSource
  }

  val flyway = bean("flyway") {
    val flyway = new Flyway()
    flyway.setDataSource(dataSource())
    flyway.migrate()
    flyway
  }

  val clientDao = bean("clientDao") {
    PostgreSqlClientDao(dataSource())
  }

  bean("clientModel") {
    ClientModel(clientDao())
  }

  val userDao = bean("userDao") {
    PostgreSqlUserDao(dataSource())
  }

  bean("userModel") {
    UserModel(userDao())
  }

  val eventDao = bean("eventDao") {
    PostgreSqlEventDao(dataSource())
  }

  bean("eventModel") {
    EventModel(eventDao())
  }
}
