package schedge

import schedge.Scraper
import schedge.models.Term
import schedge.models.Semester
import schedge.parse.Parse

fun main(args: Array<String>) {
    val s = Scraper()

    val query = s.getNyuAlbertQuery(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA",
            null,
            null,
            null,
            null
    )

    println(query)
    println(query.entity)
    val headers = query.allHeaders.map {
        "${it.name}: ${it.value}"
    }
    println(headers)
    println(query.requestLine)

    println("Trying to send request...")
    val response = s.queryNyuAlbert(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA"
    )

    println(response)

}
