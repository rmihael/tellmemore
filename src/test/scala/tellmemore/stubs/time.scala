package tellmemore.stubs.infrastructure

import org.joda.time.DateTime

import tellmemore.infrastructure.TimeProvider

case class FixedTimeProvider(now: DateTime) extends TimeProvider
