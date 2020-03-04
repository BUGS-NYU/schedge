package api;

import api.models.Course;
import api.models.Section;
import database.SelectCourses;
import io.javalin.http.Handler;
import io.javalin.plugin.openapi.dsl.OpenApiDocumentation;
import io.swagger.v3.oas.models.examples.Example;
import nyu.SubjectCode;
import nyu.Term;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static io.javalin.plugin.openapi.dsl.DocumentedContentKt.guessContentType;

class CoursesEndpoint extends Endpoint {

  enum SemesterCode {
    su,
    sp,
    fa,
    ja;
  }

  @NotNull
  @Override
  String getPath() {
    return "/:year/:semester/:school/:subject";
  }

  @NotNull
  @Override
  OpenApiDocumentation configureDocs(OpenApiDocumentation docs) {
    return docs
        .operation(openApiOperation -> {
          // openApiOperation.operationId("Operation Id");
          openApiOperation.description(
              "This endpoint returns a list of courses for a specific year, semester, school, and subject.");
          openApiOperation.summary("Courses Endpoint");
        })
        .pathParam("year", Integer.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid year.");
                   })
        .pathParam("semester", SemesterCode.class,
                   openApiParam -> {
                     openApiParam.description("Must be a valid semester code.");
                   })
        .pathParam(
            "school", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid school code. Take a look at the docs for the schools endpoint for more information.");
            })
        .pathParam(
            "subject", String.class,
            openApiParam -> {
              openApiParam.description(
                  "Must be a valid subject code. Take a look at the docs for the subjects endpoint for more information.");
            })
        .json("400", ApiError.class,
              openApiParam -> {
                openApiParam.description(
                    "One of the values in the path parameter was not valid.");
              })
        .jsonArray("200", Course.class, openApiParam -> {
          openApiParam.description("OK.");

          ArrayList<Section> sections = new ArrayList<>();

          openApiParam.getContent()
              .get(guessContentType(Course.class))
              .addExamples("course",
                           new Example().value(new Course(
                               "Intro to Computer SCI", "101",
                               new SubjectCode("CSCI", "UA"), sections)));
        });
  }

  @NotNull
  @Override
  Handler getHandler() {
    return ctx -> {
      ctx.contentType("application/json");

      int year;
      try {
        year = Integer.parseInt(ctx.pathParam("year"));
      } catch (NumberFormatException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      Term term;
      SubjectCode subject;
      try {
        term = new Term(ctx.pathParam("semester"), year);
        subject =
            new SubjectCode(ctx.pathParam("subject"), ctx.pathParam("school"));
        subject.checkValid();
      } catch (IllegalArgumentException e) {
        ctx.status(400);
        ctx.json(new ApiError(e.getMessage()));
        return;
      }

      ctx.status(200);
      ctx.json(SelectCourses.selectCourses(term, Arrays.asList(subject)));
    };
  }
}
