package models

import com.fasterxml.jackson.annotation.JsonValue
import java.time.DayOfWeek
import kotlin.experimental.and
import kotlin.experimental.or
/**
Data class describing the time of the day
 */
data class TimeOfDay(// Positive integer
    val hour: Int, val minutes: Int, val duration: Int
) {

    init {
        require(hour >= 0) { "Hour cannot be negative!" }
        require(minutes >= 0) { "Minutes cannot be negative!" }
        require(duration >= 0) { "Duration cannot be negative!" }
        require(hour <= 23) { "Hour cannot be greater than 23!" }
        require(minutes <= 59) { "Minutes cannot be greater than 59!" }
    }

    // override fun toString(): String {
    //     return "${duration} minutes starting at ${hour}:${minutes}"
    // }
}

/**
Days object
 */
class Days(vararg days: DayOfWeek) {
    private val days: Byte

    init {
        this.days = days.map { (1 shl it.value).toByte() }.reduce { daysByte, dayByte ->
            daysByte.or(dayByte)
        }
    }

    constructor(days: String) : this(*days.chunked(2).map {
        when (it) {
            "Mo" -> DayOfWeek.MONDAY
            "Tu" -> DayOfWeek.TUESDAY
            "We" -> DayOfWeek.WEDNESDAY
            "Th" -> DayOfWeek.THURSDAY
            "Fr" -> DayOfWeek.FRIDAY
            "Sa" -> DayOfWeek.SATURDAY
            "Su" -> DayOfWeek.SUNDAY
            else -> DayOfWeek.valueOf(it)
        }
    }.toTypedArray())

    /**
     * Returns a list of the DayOfWeek objects that this Days object represents.
     */
    fun toDayOfWeekList(): List<DayOfWeek> {
        return DayOfWeek.values().filter {
            ((1 shl it.value).toByte() and this.days) != 0.toByte()
        }
    }

    fun toDayNumberArray(): Array<Boolean> {
        val array = MutableList(7) { true }
        for (i in 1..7) {
            array[i % 7] = ((1 shl i).toByte() and this.days) != 0.toByte()
        }
        return array.toTypedArray()
    }

    override fun toString(): String {
        return toDayOfWeekList().toString()
    }
}

