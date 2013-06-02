package tellmemore.infrastructure.time

import org.joda.time.DateTime

case class WallClockTimeProvider() extends TimeProvider {
  def now = DateTime.now
}
