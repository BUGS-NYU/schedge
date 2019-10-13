# Schedge
Schedge is an open source course schedule solver, geared at helping NYU students
plan their courses.

### Setup
To check you work, run `gradle check`.

To run the application, fist compile it with `gradle build`, then run
`docker-compose build` to build a docker image, and `docker-compose up` to
run the application.

### Code organization
- Kotlin code handles IO
- Java code handles business logic (right now just parsing)
