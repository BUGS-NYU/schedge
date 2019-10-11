import kotlin.test.Test
import models.Schools
import models.Subjects
import models.Term
import models.Semester.*
import Scraper
import Parser

@Test fun parseCatalog() {
  val scraper = Scraper()
  val parser = Parser()
  val catalog_data = scraper.queryCourses(Term(Summer, 2019), Schools[0].abbrev, Subjects[0])
}

