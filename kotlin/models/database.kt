package models

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val sections: Array<Section>
) {

    companion object {

        @JvmStatic
        fun getSubjects(
            term: Term,
            school: String
        ): Map<Subject, Array<Course>> = database.getSubjects(term, school)

        @JvmStatic
        fun getCourses(term: Term, subject: Subject): Array<Course> = database.getCourses(term ,subject)
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
            meetings: Array<Meeting>,
            recitations: Array<Section>?
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
        val meetings: Array<Meeting>,
        val recitations: Array<Section>?
    ) : Section()

    data class Recitation(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val meetings: Array<Meeting>
    ) : Section()

    data class Lab(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val meetings: Array<Meeting>
    ) : Section()

    data class Other(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val type: SectionType,
        val meetings: Array<Meeting>
    ) : Section()
}


