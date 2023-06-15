## PSA: NYU has implemented reCAPTCHA on the current data source for Schedge.
> It is unclear if Schedge will be able to provide up-to-date data past the beginning of Spring 2023.
> Please let me know if you have found a data source other than https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL,
> and if so, I can try to rebuild the scraping code to accomodate it.

# Schedge
Schedge is an open source API to NYU's course catalog. Its goal is to help
NYU students plan their semesters' courses easier and faster.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. My goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

**API DOCS:** https://nyu.a1liu.com/api/

### NOTE: The previous version of Schedge has been deprecated, and will not have up-to-date data for semesters after Fall 2022.
The service that Schedge previously depended on was removed by NYU, and its
replacement uses an entirely different data model,
so Schedge v2 (the current version) contains breaking changes.

**Deprecated V1 API DOCS:** https://schedge.a1liu.com/

## Limitations
Data on Schedge is gotten from scraping NYU public-facing websites. This means that
the data you get from this API is not necessarily up to date with the courses on
Albert; this is mostly important for things like whether a course is open, or how
large the waitlist is. Additionally, Schedge doesn't get updates from NYU when things
change, so for example, if a professor changes a course description, and there wasn't
a scrape afterwards, Schedge won't pick it up.

## Apps that use Schedge
Here's a list of apps that use this API:

* https://nyu.myschedule.xyz/#/ NYU Course Schedule, support fall 2023 courses.

- https://rate-my-classes-pro.netlify.app | https://apple.co/3AduK8G (repo at https://github.com/zhumingcheng697/Rate-My-Classes-Pro )
- https://courses.torchnyu.com (repo at https://github.com/NicholasLYang/courses )
- https://bobcatsearch.com (repo at https://github.com/EthanPrintz/bobcat-search )
- https://notalbert.netlify.com (repo at https://github.com/pecansalad/coursecatalog )

If your project uses Schedge, please open a PR and add it to this list! You should also consider sharing
it with the [CS@NYU Discord](https://discord.com/channels/744646258819596418/744669404373188659).

## Contributing
Please take a look at [contributing guide](docs/CONTRIBUTING.md). It will provide
you a general overview of the project and how to contribute.
