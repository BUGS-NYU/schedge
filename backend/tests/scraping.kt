// import org.junit.jupiter.api.Test
import models.Schools
import models.Subjects
import models.Term
import models.Semester.*
import io.kotlintest.matchers.string.shouldBeLowerCase
import io.kotlintest.matchers.string.shouldNotBeBlank
import io.kotlintest.specs.FunSpec

class TestClass : FunSpec({
  test("Scraper should be able to initialize.") {
    Scraper()
  }
})
