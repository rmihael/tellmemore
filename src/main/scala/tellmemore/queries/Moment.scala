package tellmemore.queries

import org.joda.time.DateTime

sealed abstract class Moment {
  val tstamp: DateTime
}

object Moment {
  case class Timestamp(tstamp: DateTime) extends Moment
  case class Now(tstamp: DateTime) extends Moment
}
