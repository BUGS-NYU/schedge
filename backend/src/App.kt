package schedge

import schedge.models.Term
import schedge.models.Semester

fun main() {
    val s = Scraper()
    print("CSRFToken: ")
    println(s.csrfToken)

    val query = s.getCourseQuery(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA",
            null,
            null,
            null,
            null
    )

    print("Query: ")
    println(query)
    print("Entity: ")
    println(query.entity)
    val headers = query.allHeaders.map {
        "${it.name}: ${it.value}"
    }
    print("Headers: ")
    println(headers)

    println("Trying to send request...")
    val response = s.queryCourses(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA"
    )

    println(response)

    println(parseCourseData(response))

}
