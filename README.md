# Schedge
Schedge is an open source course schedule solver, geared at helping NYU students
plan their courses.

### Setup
To check you work, run `gradle check`.

To run the application, first compile it with `gradle build`, then run
`docker-compose build` to build a docker image, and `docker-compose up` to
run the application.

### CLI Interface
The following commands are valid in Schedge:

```sh
schedge query catalog # Query the catalog
schedge parse catalog # Parse the catalog
schedge scrape catalog # TODO scrape the catalog
schedge db add # Scrape data from the catalog and add it to the database
schedge serve # TODO serves data to API
```

### Code organization
- Kotlin code handles IO and defines most classes (`kotlin/`
  - `models` contains class definitions for modeling database contents
    - `chrono.kt` defines time-related classes
    - `api.kt` defines classes that are printed in the API
    - `nyu.kt` defines classes that describe NYU
    - `scraping.kt` defines classes that are returned from the scraper
  - `cli` contains definitions for the command-line interface
    - `Serve.kt` contains definitions of the API
  - `database` contains functions and definitions related to the database
    - `connect.kt` defines a function `connectToDatabase` which must be run
      exactly once before doing any database-related actions
    - `schema.kt` defines the schema of the database
    - `write.kt` defines how the classes in `scraping.kt` can write to the database
  - `services` defines high-level actions that Schedge can perform
- Java code handles business logic
  - Parsing
  - Shell commands
