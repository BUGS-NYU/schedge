package models

import utils.asResourceLines
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonIgnore

// @HelpWanted We need to store the subject names here as well
private val AvailableSubjects: Map<String, Set<String>> = "/subjects.txt".asResourceLines().map {
    val (subj, school) = it.split('-')
    Pair(subj, school)
}.let {
    val availSubjects = mutableMapOf<String, MutableSet<String>>()
    it.forEach { (subj, school) ->
        availSubjects.getOrPut(school) {
            mutableSetOf()
        }.add(subj)
    }
    availSubjects
}

data class SchoolMetadata internal constructor(val code: String, val name: String?)

private val Schools: List<SchoolMetadata> = "/schools.txt".asResourceLines().map {
    val (code, name) = it.split(',', limit = 2)
    SchoolMetadata(code, name)
}


/**
subjectCode class
- Companion Object
- Subject (String)
- School (String)
- Abbrev (String)
 */
class SubjectCode {

    companion object {

        @JvmStatic
        fun allSchools(): List<SchoolMetadata> = Schools

        @JvmStatic
        fun allSubjects(): List<SubjectCode> =
                AvailableSubjects.asSequence().map { pair ->
                    pair.value.asSequence().map {
                        SubjectCode(it, pair.key, Unit)
                    }
                }.flatten().toList()

        /*
        Get sequence of subject codes based on school
         */
        @JvmStatic
        fun allSubjects(forSchool: String?): List<SubjectCode> {
            if (forSchool == null)
                return allSubjects()
            val subjects = AvailableSubjects[forSchool.toUpperCase()]
            require(subjects != null) { "Must provide a valid school code!" }
            return subjects.map { SubjectCode(it, forSchool, Unit) }
        }

        @JvmStatic
        fun getUnchecked(subjectString: String): SubjectCode {
            val (subject, school) = subjectString.split('-')
            return SubjectCode(subject, school, Unit)
        }

        @JvmStatic
        fun getUnchecked(subject: String, school: String): SubjectCode {
            return SubjectCode(subject, school, Unit)
        }
    }

    val subject: String
    val school: String
    val abbrev: String
        @JsonIgnore
        inline get() {
            return "$subject-$school"
        }

    // No constructor without checks
    private constructor(abbrevString: String, schoolString: String, unused: Unit) {
        subject = abbrevString.toUpperCase()
        school = schoolString.toUpperCase()
        unused.hashCode() // To silence errors
    }

    constructor(abbrevString: String, schoolString: String) {
        subject = abbrevString.toUpperCase()
        school = schoolString.toUpperCase()
        val subjects = AvailableSubjects[school]
        require(subjects != null) { "School must be valid" }
        require(subjects.contains(subject)) { "Subject must be valid!" }
    }

    override fun toString(): String = abbrev

    override fun hashCode(): Int {
        return abbrev.hashCode()
    }
}

