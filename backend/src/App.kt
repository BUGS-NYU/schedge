package schedge

import schedge.Scraper
import schedge.models.Term
import schedge.models.Semester
import schedge.parse.Parse

fun main(args: Array<String>) {
    println(Term(Semester.Summer, 2019))
    val s = Scraper()

    val response = s.queryNyuAlbert(
      Term(Semester.Fall, 2019),
      "UA",
      "MATH-UA"
    )

    println(response)
}
