package tellmemore

import org.scala_tools.time.Imports._

package object users {
  case class UserId(clientId: String, externalId: String)

  case class User(id: UserId, humanReadableId: String, created: DateTime)
}
