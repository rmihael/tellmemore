package tellmemore.clients

import org.joda.time.DateTime

case class Client(id: String, name: String, created: DateTime, last_login: DateTime)
