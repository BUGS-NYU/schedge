package schedge

import schedge.Scraper
import schedge.models.Term
import schedge.models.Semester
import schedge.parse.Parse

fun main(args: Array<String>) {
    println(Term(Semester.Summer, 2019))
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

    val response = s.queryNyuAlbert(
            Term(Semester.Fall, 2019),
            "UA",
            "MATH-UA"
    )

    println(response)

}
