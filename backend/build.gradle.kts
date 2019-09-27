import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
  // Apply the Kotlin JVM plugin to add support for Kotlin.
  kotlin("jvm") version "1.3.41"

  // Apply the application plugin to add support for building a CLI application.
  application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Standard library
  implementation(kotlin("stdlib"))

  // Web requests framework
  implementation("org.apache.httpcomponents:httpclient:4.5.10")

  // HTML Parsing
  implementation("org.jsoup:jsoup:1.10.3")

  // Database framework
  implementation("org.jetbrains.exposed:exposed:0.17.3")

  // Web server framework
  implementation("io.javalin:javalin:3.5.0")

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
  kotlin.srcDir("src")
}

application {
  // Define the main class for the application
  mainClassName = "schedge.AppKt"
  sourceSets["main"].java.srcDir("java")
}
