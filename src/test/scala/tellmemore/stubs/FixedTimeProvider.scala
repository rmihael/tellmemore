package tellmemore.stubs

import org.joda.time.DateTime

import tellmemore.infrastructure.time.TimeProvider

case class FixedTimeProvider(now: DateTime = DateTime.now) extends TimeProvider
