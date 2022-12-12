# Schedge
Schedge is an open source API to NYU's course catalog. Its goal is to help
NYU students plan their semesters' courses easier and faster.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. My goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

**API DOCS:** https://nyu.a1liu.com/api/

### NOTE: The previous version of Schedge has been deprecated, and will not have up-to-date data for semesters after Fall 2022.
The service that Schedge v1 depended on was removed by NYU, and uses an entirely different data model,
so Schedge v2 (the current version) contains breaking changes.

**Deprecated V1 API DOCS:** https://schedge.a1liu.com/

## Limitations
Data on Schedge is gotten from scraping NYU public-facing websites. This means that
the data you get from this API is not necessarily up to date with the courses on
Albert; this is mostly important for things like whether a course is open, or how
large the waitlist is. Additionally, Schedge doesn't get updates from NYU when things
change, so for example, if a professor changes a course description, and there wasn't
a scrape afterwards, Schedge won't pick it up.

## Front Ends
Here's a list of frontends that people have written for this API.

- https://courses.torchnyu.com (repo at https://github.com/NicholasLYang/courses )
- https://bobcatsearch.com (repo at https://github.com/EthanPrintz/bobcat-search )
- https://notalbert.netlify.com (repo at https://github.com/pecansalad/coursecatalog )

If you've written a front end to this API, you can open a PR and add yourself to this list!

## Contributing
Please take a look at [contributing guide](docs/CONTRIBUTING.md). It will provide
you a general overview of the project and how to contribute.
