# Schedge
Schedge is an open source API to NYU's course catalog, geared at eventually helping
NYU students plan their courses easier and faster.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. Our goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

API Docs: http://schedge.a1liu.com

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
The API documentation is available at http://schedge.a1liu.com .
