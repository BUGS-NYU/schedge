package cli.db

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.*
import models.Course
import models.Subject
import models.Term

internal class DbGet : CliktCommand(name = "get") {
    private val term by option("--term").convert { Term.fromId(it.toInt()) }.required()
    private val subjectOrSchool by mutuallyExclusiveOptions(
        option("--subject").convert { Pair(it, true) },
        option("--school").convert { Pair(it, false) }
    ).required()

    override fun run() {
        val isSubject = subjectOrSchool.second
        if (isSubject) {
            Course.getCourses(term, Subject(subjectOrSchool.first))
        } else {
            Course.getSubjects(term, subjectOrSchool.first)
        }

    }
}