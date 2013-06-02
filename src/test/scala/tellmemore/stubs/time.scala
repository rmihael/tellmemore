package tellmemore.stubs.infrastructure

import org.joda.time.DateTime

import tellmemore.infrastructure.time.TimeProvider

case class FixedTimeProvider(now: DateTime) extends TimeProvider
