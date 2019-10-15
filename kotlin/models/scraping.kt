package models

data class CatalogEntry(
    val courseName: String,
    val subject: String,
    val courseId: Int,
    val deptCourseNumber: Int,
    val sections: List<CatalogSectionEntry>
)

data class CatalogSectionEntry(
    val registrationNumber: Int,
    val type: SectionType,
    val associatedWith: Int?,
    val meetings: List<Meeting>
)

data class SectionResult(
  val description: String
)
