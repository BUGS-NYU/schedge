package models

class Course(
    val subject: Subject,
    val name: String,
    val deptCourseNumber: Int,
    val sections: Array<Section>
)

class Section(
    val registrationNumber: Int,
    val sectionNumber: Int,
    val instructor: String,
    val type: SectionType,
    val associatedWith: Section?
    val meetings: Array<Meeting>
)

