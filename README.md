# Schedge
Schedge is an open source course schedule solver, geared at helping NYU students
plan their courses.

API: http://schedge.a1liu.com

## CLI Interface
The following commands are valid in Schedge:

```sh
./schedge query catalog # Query the catalog
./schedge parse catalog # Parse the catalog
./schedge scrape catalog # scrape the catalog
./schedge db scrape # scrape the catalog for information
./schedge db query # query the database for information
./schedge db serve # rehash of previous javalin project
```

## API Documentation
The API is available at http://schedge.a1liu.com . Note that **the API is not case
sensitive.**

### Courses Endpoint - `/:year/:semester/:school/:subject`
This endpoint returns courses in a given subject during a given semester. The values
above are:

- `:year` is the year
- `:semester` is the first two letters of the semester, so for summer it's `su`,
  for J-term it's `ja`, Spring is `sp`, Fall is `fa`.
- `:school` is the school code, the full list of which can be obtained by using
  the `/schools` endpoint.
- `:subject` is the subject code, the full list of which can be obtained by using
  the `/subjects` endpoint.

Example: `schedge.a1liu.com/2020/sp/ua/csci` returns the Computer Science courses
for the Fall 2020 school year in the College of Arts and Sciences.

**This endpoint is not finalized yet.**

### Schools Endpoint - `/schools`
This endpoint returns the school codes and their names as a list of objects, which
follow the following format:

```javascript
{
  "code":"UA", // This is the code used in the courses endpoint
  "name":"College of Arts and Sciences"
}
```

### Subjects Endpoint - `/subjects`
This endpoint returns all subjects as a list of objects with the following format:

```javascript
{
  "school":"UA", // Used in the courses endpoint for schools
  "subject":"CSCI" // Used in the courses endpoint for subjects
}
```

### Filtered Subjects Endpoint - `/subjects/:school`
This endpoint returns all subjects for a given school as a list of objects with
the following format:

```javascript
{
  "school":"UA", // Used in the courses endpoint for schools
  "subject":"MATH" // Used in the courses endpoint for subjects
}
```

Example: `schedge.a1liu.com/subjects/UA` returns all subjects that the College
of Arts and Sciences offers classes under.

