# Schedge
Schedge is an open source API to NYU's course catalog, geared at eventually helping
NYU students plan their courses easier and faster.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. Our goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

API Docs: https://schedge.a1liu.com

## Contributing
Please take a look at [contributing guide](docs/CONTRIBUTING.md). It will provide
you a general overview of the project and how to contribute.

## API Documentation
The API documentation is available at https://schedge.a1liu.com .

## TODO
- Fix bugs reported in issues
- Remove all the different `models` folders, simplify database edits and whatnot
- Deploy with CI/CD
- Fix time formatting/time zone stuffs
- Make schools list dynamic
- Make a dedicated frontend
- Improve 1st query performance (3-4 seconds is concerning, even if its only during
  warm-up)
- Add full RMP support

## Commands
The following commands are valid in Schedge:

```sh
./schedge query catalog     # Query the catalog
./schedge query sections    # Query a section
./schedge query rmp         # Query all rate my prof reviews
./schedge query school      # Query a school/subject
./schedge parse catalog     # Parse the catalog
./schedge parse section     # Parse the section
./schedge parse school      # Parse the school/subject
./schedge scrape catalog    # scrape the catalog
./schedge scrape sections   # scrape the sections
./schedge scrape school     # scrape the school/subject
./schedge db scrape         # scrape the catalog for information
./schedge db rmp            # scrape all rate my prof reviews
./schedge db query          # query the database for information
./schedge db serve          # Serve the database data through an API
```

#### Example
```sh
./schedge scrape catalog --year 2020 --semester sp --school UA --subject CSCI
```
The above command will scrape catalog data from Albert website for Computer Science courses at the
College of Arts and Sciences for the Spring, 2020.

## Front Ends
If you'd like to write a front end to this API, you can list it below!

- https://courses.torchnyu.com (repo at https://github.com/NicholasLYang/courses )
- https://bobcatsearch.com (repo at https://github.com/EthanPrintz/bobcat-search )
- https://notalbert.netlify.com (repo at https://github.com/pecansalad/coursecatalog )
