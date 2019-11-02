package models

class Course {

    val name: String
    val deptCourseNumber: Int
    val sections: Array<Section>

    companion object {
        fun getCourses(subject: Subject, term: Term): Array<Course> {
            return arrayOf()
        }
    }

    private constructor(
        name: String,
        deptCourseNumber: Int,
        sections: Array<Section>
    ) {
        this.name = name
        this.deptCourseNumber = deptCourseNumber
        this.sections = sections
    }
}

class Section(
    val registrationNumber: Int,
    val sectionNumber: Int,
    val instructor: String,
    val type: SectionType,
    val associatedWith: Section?,
    val meetings: Array<Meeting>
)

