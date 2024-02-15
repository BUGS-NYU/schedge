## PSA: NYU has implemented reCAPTCHA on the current data source for Schedge.
> The scraper for semesters after the Captcha was added is in progress, but will take time.
> Please let BUGS know if you have found a data source other than https://sis.nyu.edu/psc/csprod/EMPLOYEE/SA/c/NYU_SR.NYU_CLS_SRCH.GBL,
> and if so, we we may be able to provide data sooner.

# Schedge
Schedge is an open source API to NYU's course catalog. Its goal is to help
NYU students plan their semesters' courses easier and faster.

NYU Albert is really annoying to use. The goal with this API is to make it easier for students to plan
their schedules, and eventually to also do some of that for them.

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

- https://nyu.myschedule.xyz/#/ NYU Course Schedule, support fall 2023 courses
- https://rate-my-classes-pro.netlify.app | https://apple.co/3AduK8G (repo at https://github.com/zhumingcheng697/Rate-My-Classes-Pro )
- https://courses.torchnyu.com (repo at https://github.com/NicholasLYang/courses )
- https://bobcatsearch.com (repo at https://github.com/EthanPrintz/bobcat-search )
- https://notalbert.netlify.com (repo at https://github.com/pecansalad/coursecatalog )
- NYU Course Auto Scheduler (repo at https://github.com/rithkott/NYU-class-scheduler)

If your project uses Schedge, please open a PR and add it to this list! You should also consider sharing
it with the [CS@NYU Discord](https://discord.com/channels/744646258819596418/744669404373188659).

## Contributing
Please take a look at [contributing guide](docs/CONTRIBUTING.md). It will provide
you a general overview of the project and how to contribute.

We value your feedback! If you encounter any issues or have suggestions for improving Schedge, please don't hesitate to reach out to us. Your input helps us enhance the quality and reliability of the data provided by Schedge.

Stay updated on the latest changes to Schedge! You can subscribe to notifications or check for announcements on our website to receive alerts about updates to the data source and any improvements to Schedge.

We extend our gratitude to all contributors who have helped enhance Schedge and provide valuable feedback. Your contributions are invaluable in making Schedge a more reliable and efficient tool for NYU students.

