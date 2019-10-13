package models

data class CatalogEntry(
    val courseName: String,
    val subject: String,
    val courseId: Int,
    val deptCourseNumber: Int,
    val sectionRegistrationNumbers: List<Int>,
    val sectionTypes: List<SectionType>
)

data class SectionResult(
  val courseName: String,
  val type: SectionType,
  val associatedWith: Int?,
  val meetings: List<Meeting>
)
