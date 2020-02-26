package services

import models.SubjectCode
import models.Term
import scraping.SimpleBatchedFutureEngine
import scraping.models.Course
import scraping.models.School
import scraping.models.Section
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min
import kotlin.streams.asStream
import kotlin.streams.toList

fun masterScrapeSection(term: Term, subjectCode: SubjectCode): List<Course> {
    val courses = scrapeFromCatalog(term, subjectCode)
    val iterator = courses.stream().map { course ->
        course.sections
    }.flatMap { mutableList ->
        mutableList.stream()
    }.iterator()

    val size = courses.map { course ->
        course.sections
    }.flatten().toList().size

    SimpleBatchedFutureEngine<Section, Void>(
            iterator, size
    ) { section, _ ->
        section.update(querySection(term, section.registrationNumber))
    }

    return courses
}

fun masterScrapeSection(term: Term, forSchool: String, batchSizeNullable: Int?): Sequence<List<Course>> {
    val courses = scrapeFromCatalog(term, SubjectCode.allSubjects(forSchool), batchSizeNullable)
    val iterator = courses.flatten().asStream().map { course ->
        course.sections
    }.flatMap { mutableList ->
        mutableList.stream()
    }.iterator()

    val size = courses.flatten().map { course ->
        course.sections
    }.flatten().toList().size
    SimpleBatchedFutureEngine<Section, Void>(
            iterator, size
    ) { section, _ ->
        section.update(querySection(term, section.registrationNumber))
    }
    return courses
}


fun masterScrapeSection(term: Term, subjectCodes: List<SubjectCode>, batchSizeNullable: Int?): Sequence<List<Course>> {
    val courses = scrapeFromCatalog(term, subjectCodes, batchSizeNullable)
    val iterator = courses.flatten().asStream().map { course ->
        course.sections
    }.flatMap { mutableList ->
        mutableList.stream()
    }.iterator()

    val size = courses.flatten().map { course ->
        course.sections
    }.flatten().toList().size
    SimpleBatchedFutureEngine<Section, Void>(
            iterator, size
    ) { section, _ ->
        section.update(querySection(term, section.registrationNumber))
    }
    return courses
}

private fun scrapeCourseSections(term: Term, courses: Sequence<Course>, batchSizeNullable: Int? = null): List<Course> {
    val courseList: MutableList<Course> = mutableListOf()
    val iterator = courses.map { course ->
        courseList.add(course)
        course.sections
    }.flatMap { mutableList ->
        mutableList.asSequence()
    }.iterator()

    val batchSize = batchSizeNullable ?: 20 // @Performance What should this number be?

    SimpleBatchedFutureEngine(iterator, batchSize) { section, _ ->
        section.update(querySection(term, section.registrationNumber))
    }.forEach { _ -> }
    return courseList
}