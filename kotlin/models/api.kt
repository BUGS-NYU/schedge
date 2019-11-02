package models

// Uses YACS API

class YacsCourse(
    val shortname: Int, // deptCourseNumber
    val longname: String, // name
    val sections: Array<Section>
){
}

class YacsSection constructor(
    val crn: Int, // registrationNumber
    val shortname: Int, // sectionNumber
    val instructors: String, // instructor
    val type: SectionType, // Not part of YACS API
    val periods: Array<YacsPeriod>
)

class YacsPeriod private constructor()

