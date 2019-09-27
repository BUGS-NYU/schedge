package schedge.models

import kotlin.experimental.or
import kotlin.experimental.and

class TimeOfDay {

  // Positive integer
  private val hour: Int
  private val minutes: Int
  private val minutesLong: Int

  constructor(hour: Int, minutes: Int, minutesLong: Int) {
    require(hour >= 0) { "Hour cannot be negative!" }
    require(minutes >= 0) { "Minutes cannot be negative!" }
    require(minutesLong >= 0) { "MinutesLong cannot be negative!" }

    require(hour <= 23) { "Hour cannot be greater than 23!" }
    require(minutes <= 59) { "Minutes cannot be greater than 59!" }

    this.hour = hour
    this.minutes = minutes
    this.minutesLong = minutesLong
  }
}

class Days(vararg days: DayOfWeek) {
  private val days : Byte

  init {
    this.days = days.map { it.toByte() }.reduce {
      daysByte, dayByte -> daysByte.or(dayByte)
    }
  }

  /**
   * Returns a list of the DayOfWeek objects that this Days object represents.
   */
  private fun toDayOfWeekList(): List<DayOfWeek> {
    return DayOfWeek.values().filter {
      (it.toByte() and this.days) != 0.toByte()
    }
  }

  override fun toString(): String {
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
      Sunday -> 0
      Monday -> 1
      Tuesday -> 2
      Wednesday -> 3
      Thursday -> 4
      Friday -> 5
      Saturday -> 6
    }
    return ret.toByte()
  }

//  fun toShortString(): String {
//    return this.toString().substring(0, 2)
//  }
}
