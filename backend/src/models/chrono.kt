package schedge.models

import kotlin.experimental.or
import kotlin.experimental.and

class TimeOfDay {

  val hour: Int
  val minutes: Int
  val minutesLong: Int

  constructor(hour: Int, minutes: Int, minutesLong: Int) {
    if (hour < 0) throw IllegalArgumentException("Hour cannot be negative!")
    if (minutes < 0) throw IllegalArgumentException("Minutes cannot be negative!")
    if (minutesLong < 0) throw IllegalArgumentException("MinutesLong cannot be negative!")

    this.hour = hour
    this.minutes = minutes
    this.minutesLong = minutesLong
  }
}

class Days {
  val days : Byte

  constructor(vararg days: DayOfWeek) {
    this.days = days.map { it.toByte() }.reduce {
      daysByte, dayByte -> daysByte.or(dayByte)
    }
  }

  /**
   * Returns a list of the DayOfWeek objects that this Days object represents.
   */
  public fun toDayOfWeekList(): List<DayOfWeek> {
    return DayOfWeek.values().filter {
      (it.toByte() and this.days) != 0.toByte()
    }
  }

  public override fun toString(): String {
    return toDayOfWeekList().toString()
  }
}

enum class DayOfWeek {
  Sunday,
  Monday,
  Tuesday,
  Wednesday,
  Thursday,
  Friday,
  Saturday;

  fun toByte(): Byte {
    val ret = 1 shl when (this) {
      DayOfWeek.Sunday -> 0
      DayOfWeek.Monday -> 1
      DayOfWeek.Tuesday -> 2
      DayOfWeek.Wednesday -> 3
      DayOfWeek.Thursday -> 4
      DayOfWeek.Friday -> 5
      DayOfWeek.Saturday -> 6
    }
    return ret.toByte()
  }

  fun toShortString(): String {
    return this.toString().substring(0, 2)
  }
}
