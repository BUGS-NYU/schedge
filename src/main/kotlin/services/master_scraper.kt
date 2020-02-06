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

fun masterScrapeSection(term : Term, subjectCode: SubjectCode, batchSizeNullable: Int?) {
    val courses = scrapeFromCatalog(term, subjectCode)
    val registrationNumbers = courses.stream().map {
        course -> course.sections.map { section ->
        section.registrationNumber
    }.toList()
    }.flatMap {
        list -> list.stream()
    }.toList()

    val iterator: MutableIterator<Section> = courses.stream().map {
        course -> course.sections
    }.flatMap {
        mutableList -> mutableList.stream()
    }.iterator()

    val batchSize = batchSizeNullable ?: max(5, min(registrationNumbers.size / 5, 20))

    SimpleBatchedFutureEngine<Section, Void>(
            iterator, batchSize
    ){
        section, _ -> section.update(querySection(term, section.registrationNumber))
    }
}

fun masterScrapeSection(term : Term, forSchool: String, batchSizeNullable: Int?) {
    val courses = scrapeFromCatalog(term, SubjectCode.allSubjects(forSchool),batchSizeNullable)
    val iterator = courses.flatten().asStream().map {
        course -> course.sections
    }.flatMap {
        mutableList -> mutableList.stream()
    }.iterator()
//    val batchSize = batchSizeNullable ?: max(5, min(SubjectCode.allSubjects(forSchool).size / 5, 20))
    val size = courses.flatten().toList().size
    SimpleBatchedFutureEngine<Section, Void>(
            iterator, size
    ){
        section, _ -> section.update(querySection(term, section.registrationNumber))
    }
}

//Change batchSize
fun masterScrapeSection(term : Term, subjectCodes: List<SubjectCode>, batchSizeNullable: Int?) {
    val courses = scrapeFromCatalog(term, subjectCodes, batchSizeNullable)
    val iterator = courses.flatten().asStream().map {
        course -> course.sections
    }.flatMap {
        mutableList -> mutableList.stream()
    }.iterator()
    val batchSize = batchSizeNullable ?: max(5, min(subjectCodes.size / 5, 20))
    SimpleBatchedFutureEngine<Section, Void>(
            iterator,batchSize
    ){
        section, _ -> section.update(querySection(term, section.registrationNumber))
    }
}