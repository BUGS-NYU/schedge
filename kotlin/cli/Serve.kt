package cli

import io.javalin.Javalin
import com.github.ajalt.clikt.core.CliktCommand

internal class Serve : CliktCommand(name = "serve") {

    override fun run(): Unit {
        val app = Javalin.create().start(8080)
        app.get("/") { ctx -> ctx.result("Hello World") }
        // Query params subject, termid or subject, semester, year
        // app.get("/catalog") { ctx -> ctx.result("Hello World") }
        // Query params subject, termid, course name, school
        // app.get("/courses") { ctx -> ctx.result("Hello World") }
    }
}
