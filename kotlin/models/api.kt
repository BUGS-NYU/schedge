package models

data class Course(
    val nyuCourseId: Long,
    val name: String,
    val deptCourseNumber: Long,
    val sections: List<Section>
) {

    companion object {

    }
}

sealed class Section(val type: SectionType) {

    companion object {

        @JvmStatic
        fun getSection(
            registrationNumber: Int,
            sectionNumber: Int,
            instructor: String,
            type: SectionType,
            status: SectionStatus,
            meetings: List<Meeting>,
            recitations: List<Section>?
        ): Section {
            return when (type) {
                SectionType.LEC -> Lecture(registrationNumber, sectionNumber, instructor, status, meetings, recitations)
                SectionType.RCT -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Recitation(registrationNumber, sectionNumber, instructor, status, meetings)
                }
                SectionType.LAB -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Lab(registrationNumber, sectionNumber, instructor, status, meetings)
                }
                else -> {
                    require(recitations == null) { "Provided argument recitations=$recitations when type was not SectionType.LEC." }
                    Other(registrationNumber, sectionNumber, instructor, status, type, meetings)
                }
            }
        }
    }

    data class Lecture(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>,
        val recitations: List<Section>?
    ) : Section(SectionType.LEC)

    data class Recitation(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.RCT)

    data class Lab(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        val meetings: List<Meeting>
    ) : Section(SectionType.LAB)

    class Other(
        val registrationNumber: Int,
        val sectionNumber: Int,
        val instructor: String,
        val status: SectionStatus,
        type: SectionType,
        val meetings: List<Meeting>
    ) : Section(type)
}

