package tellmemore.clients

import org.springframework.scala.jdbc.core.JdbcTemplate
import org.springframework.dao.EmptyResultDataAccessException
import org.scala_tools.time.Imports._

case class ClientDao(jdbcTemplate: JdbcTemplate) {
  def getById(id: String): Either[Exception, Option[Client]] = {
    try {
      Right(jdbcTemplate.queryForObjectAndMap("SELECT id, name, created, last_login FROM clients WHERE id=?", id) {
        (resultSet, _) => Client(resultSet.getInt(1).toString, resultSet.getString(2),
                                 new DateTime(resultSet.getInt(3) * 1000L), new DateTime(resultSet.getInt(4) * 1000L))
      })
    } catch {
      case _ :EmptyResultDataAccessException => Right(None)
      case exc: Exception => Left(exc)
    }
  }
}
