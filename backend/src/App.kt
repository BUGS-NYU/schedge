package schedge

import java.io.IOException
import schedge.models.Term
import schedge.models.Semester
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import schedge.Logging

class App(): CliktCommand() {
  private val loggingLevel by option(help="The logging level of the application.").int().default(Logging.WARN)

  override fun run() {
      Logging.loggingLevel = loggingLevel
      Logging.info("Logging level set to ${loggingLevel} (${when (loggingLevel) {
          1 -> "debug"
          2 -> "info"
          3 -> "warn"
          4 -> "error"
          else -> throw IOException("Logging level set to invalid value.")
      }})")
  }
}

fun main(args: Array<String>) = App().main(args)

fun hi() {
    val s = Scraper()

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
