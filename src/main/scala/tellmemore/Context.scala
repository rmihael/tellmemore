// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// !!! Currently this file is completely unused. After releasing of Spring-Scala-M3 we can try to get back to functional
// configuration, but until then we're using XML-based configuration for it's context:component-scan
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

package tellmemore

import org.apache.commons.dbcp.BasicDataSource
import org.springframework.jdbc.datasource.{TransactionAwareDataSourceProxy, LazyConnectionDataSourceProxy,
                                            DataSourceTransactionManager}
import org.springframework.scala.context.function.{FunctionalConfiguration}
import com.googlecode.flyway.core.Flyway

import tellmemore.clients.{PostgreSqlClientDao, ClientModel}
import tellmemore.events.{EventModel, PostgreSqlEventDao}
import tellmemore.userfacts.{PostgreSqlUserFactDao, UserFactModel}
import tellmemore.users.{UserModel, PostgreSqlUserDao}
import javax.sql.DataSource
import tellmemore.infrastructure.time.WallClockTimeProvider

trait ITDataSourceConfiguration extends FunctionalConfiguration {
  lazy val dataSource = singleton("dataSource") {
    val dataSource = new BasicDataSource()
    dataSource.setDriverClassName("org.h2.Driver")
    dataSource.setUrl("jdbc:h2:mem:tellmemore;MODE=POSTGRESQL")
    new TransactionAwareDataSourceProxy(new LazyConnectionDataSourceProxy(dataSource))
  }
}

trait DataSourceConfiguration extends FunctionalConfiguration {
  lazy val dataSource = singleton("dataSource") {
    val dataSource = new BasicDataSource()
    dataSource.setDriverClassName("org.h2.Driver")
    dataSource.setUrl("jdbc:h2:mem:tellmemore;MODE=POSTGRESQL")
    new TransactionAwareDataSourceProxy(new LazyConnectionDataSourceProxy(dataSource))
  }
}

abstract class BaseContext extends FunctionalConfiguration {
  val dataSource: DataSource

  val txManager = bean("transactionManager") {
    val txManager = new DataSourceTransactionManager()
    txManager.setDataSource(dataSource)
    txManager
  }

  val timeProvider = bean("timeProvider") { WallClockTimeProvider() }

  val flyway = bean("flyway") {
    val flyway = new Flyway()
    flyway.setDataSource(dataSource)
    flyway
  } init {
    _.migrate()
  }

  val clientDao = bean("clientDao") {
    PostgreSqlClientDao(dataSource)
  }

  bean("clientModel") {
    ClientModel(clientDao(), txManager())
  }

  val userDao = bean("userDao") {
    PostgreSqlUserDao(dataSource)
  }

  bean("userModel") {
    UserModel(userDao(), txManager())
  }

  val eventDao = bean("eventDao") {
    PostgreSqlEventDao(dataSource)
  }

  bean("eventModel") {
    EventModel(eventDao(), txManager())
  }

  val userFactDao = bean("userFactDao") {
    PostgreSqlUserFactDao(dataSource)
  }

  bean("userFactModel") {
    UserFactModel(userFactDao(), txManager(), timeProvider())
  }
}

class ApplicationContext extends BaseContext with DataSourceConfiguration

class ITContext extends BaseContext with ITDataSourceConfiguration
