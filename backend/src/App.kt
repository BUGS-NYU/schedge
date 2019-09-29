package schedge

import schedge.Scraper
import schedge.models.Term
import schedge.models.Semester
import schedge.parse.Parse

fun main(args: Array<String>) {
    val s = Scraper()
    print("CSRFToken: ")
    println(s.csrfToken)

    val query = s.getNyuAlbertQuery(
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
    val response = s.queryNyuAlbert(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA"
    )

    println(response)

}
