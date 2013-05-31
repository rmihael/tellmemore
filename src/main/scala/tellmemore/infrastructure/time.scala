package tellmemore.infrastructure

import org.joda.time.DateTime

trait TimeProvider {
  def now: DateTime
}

case class WallClockTimeProvider() extends TimeProvider {
  def now = DateTime.now
}
