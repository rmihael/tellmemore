package tellmemore.infrastructure.time

import org.joda.time.DateTime

trait TimeProvider {
  def now: DateTime
}
