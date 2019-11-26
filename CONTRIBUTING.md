# Contributing

### Setup
To check you work, run `gradle check`.

To run the application, first compile it with `gradle build`, then run
`docker-compose build` to build a docker image, and `docker-compose up` to
run the application.

### Code organization
- `models` contains class definitions for modeling data
  - `chrono.kt` defines time-related classes
  - `api.kt` defines classes that are printed in the API
  - `nyu.kt` defines classes that describe NYU
- `cli` contains definitions for the command-line interface
- `services` defines high-level actions that Schedge can perform
  - `query_service.kt` queries NYU Albert
  - `scrape_service.kt` scrapes NYU Albert
  - `JsonMapper.java` converts data structures to JSON
  - `ParseCatalog.java` parses the catalog data

### Annotations
The codebase uses the following annotations in the comments:

- `@HelpWanted` - We need contributors for this code
- `@TODO` - We need to finish this code
