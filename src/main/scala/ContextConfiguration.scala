import org.apache.commons.dbcp.BasicDataSource
import org.springframework.jdbc.datasource.{TransactionAwareDataSourceProxy, LazyConnectionDataSourceProxy,
                                            DataSourceTransactionManager}
import org.springframework.scala.context.function.FunctionalConfiguration
import com.googlecode.flyway.core.Flyway

import tellmemore.clients.{PostgreSqlClientDao, ClientModel}
import tellmemore.events.{EventModel, PostgreSqlEventDao}
import tellmemore.userfacts.{PostgreSqlUserFactDao, UserFactModel}
import tellmemore.users.{UserModel, PostgreSqlUserDao}

class ContextConfiguration extends FunctionalConfiguration {
  val dataSource = bean("dataSource") {
    val dataSource = new BasicDataSource()
    dataSource.setDriverClassName("org.h2.Driver")
    dataSource.setUrl("jdbc:h2:mem:tellmemore;MODE=POSTGRESQL")
    new TransactionAwareDataSourceProxy(new LazyConnectionDataSourceProxy(dataSource))
  }

  val txManager = bean("transactionManager") {
    val txManager = new DataSourceTransactionManager()
    txManager.setDataSource(dataSource())
    txManager
  }

  val flyway = bean("flyway") {
    val flyway = new Flyway()
    flyway.setDataSource(dataSource())
    flyway
  } init {
    _.migrate()
  }

  val clientDao = bean("clientDao") {
    PostgreSqlClientDao(dataSource())
  }

  bean("clientModel") {
    ClientModel(clientDao(), txManager())
  }

  val userDao = bean("userDao") {
    PostgreSqlUserDao(dataSource())
  }

  bean("userModel") {
    UserModel(userDao(), txManager())
  }

  val eventDao = bean("eventDao") {
    PostgreSqlEventDao(dataSource())
  }

  bean("eventModel") {
    EventModel(eventDao(), txManager())
  }

  val userFactDao = bean("userFactDao") {
    PostgreSqlUserFactDao(dataSource())
  }

  bean("userFactModel") {
    UserFactModel(userFactDao(), txManager())
  }
}
