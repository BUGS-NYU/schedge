# Schedge
Schedge is an open source course schedule solver, geared at helping NYU students
plan their courses.

API: http://schedge.a1liu.com

Please note that the database schema is currently undergoing changes and thus in
the short term, the API will be unreliable.

### CLI Interface
The following commands are valid in Schedge:

```sh
schedge query catalog # Query the catalog
schedge parse catalog # Parse the catalog
schedge scrape catalog # scrape the catalog
schedge db scrape # scrape the catalog for information
schedge db query # query the database for information
schedge serve api # TODO rehash of previous javalin project
schedge serve yacs # TODO rehash of previous javalin project, w/ YACS api
```
