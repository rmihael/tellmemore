import org.apache.commons.dbcp.BasicDataSource
import org.springframework.scala.context.function.FunctionalConfiguration
import org.springframework.scala.jdbc.core.JdbcTemplate
import com.googlecode.flyway.core.Flyway

import tellmemore.clients.{ClientDao, ClientModel}

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

  val jdbcTemplate = bean("jdbcTemplate") {
    val depends = Seq(flyway)
    new JdbcTemplate(dataSource())
  }

  val clientDao = bean("clientDao") {
    ClientDao(jdbcTemplate())
  }

  bean("clientModel") {
    ClientModel(clientDao())
  }
}
