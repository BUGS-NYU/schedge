package models

import org.joda.time.DateTime
import org.joda.time.Duration

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val sections: List<Section>
) {

    companion object {

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

