package tellmemore.infrastructure

import java.sql.Connection
import javax.sql.DataSource

object DB {
  def withConnection[A](dataSource: DataSource)(block: Connection => A): A = {
    val connection = dataSource.getConnection
    block(connection)
  }
}
