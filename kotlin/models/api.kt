package models

import org.joda.time.DateTime
import org.joda.time.Duration

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val sections: Array<Section>
) {

    companion object {

    }

    override fun equals(other: Any?): Boolean { // AUTO GENERATED
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Course

        if (nyuCourseId != other.nyuCourseId) return false
        if (name != other.name) return false
        if (deptCourseNumber != other.deptCourseNumber) return false
        if (!sections.contentEquals(other.sections)) return false

        return true
    }

    override fun hashCode(): Int { // AUTO GENERATED
        var result = nyuCourseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + deptCourseNumber.toInt()
        result = 31 * result + sections.contentHashCode()
        return result
    }
}

sealed class Section {

    companion object {
        fun getSection(
            registrationNumber: Int,
            sectionNumber: Int,
            instructor: String,
            type: SectionType,
            meetings: List<Meeting>,
            recitations: List<Section>?
        ): Section {
            return when (type) {
                SectionType.LEC -> Lecture(registrationNumber, sectionNumber, instructor, meetings, recitations)
                SectionType.RCT -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Recitation(registrationNumber, sectionNumber, instructor, meetings)
                }
                SectionType.LAB -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Lab(registrationNumber, sectionNumber, instructor, meetings)
                }
                else -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Other(registrationNumber, sectionNumber, instructor, type, meetings)
                }
            }
        }
    }

    data class Lecture(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val meetings: List<Meeting>,
        val recitations: List<Section>?
    ) : Section()

    data class Recitation(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val meetings: List<Meeting>
    ) : Section()

    data class Lab(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val meetings: List<Meeting>
    ) : Section()

    data class Other(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val type: SectionType,
        val meetings: List<Meeting>
    ) : Section()
}

