# Schedge
Schedge is an open source API to NYU's course catalog, geared at eventually helping
NYU students plan their courses easier and faster.

### NOTE: NYU has removed the service that Schedge v1 relies on. Its replacement uses an entirely different data model, so I'll be releasing a version 2 of Schedge with breaking changes, both to fix some weirdness from v1 and also to update Schedge to the new data model.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. Our goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

**V1 API DOCS:** https://schedge.a1liu.com

## Contributing
Please take a look at [contributing guide](docs/CONTRIBUTING.md). It will provide
you a general overview of the project and how to contribute.

## TODO
- [ ] Fix bugs reported in issues
- [ ] Fix time formatting/time zone stuffs
- [ ] Make a first-party frontend
- [ ] Fix search, it's super garbo right now
- [x] Remove epochs? Turns out there's no need for them, we can just use transactions
- [x] Deploy with CI/CD (`nyu.a1liu.com`)
- [x] Make the schools/subjects list more dynamic
- [x] Term should use an enum instead of hard-coded integers
- [x] Schools/Subjects/whatever should be in one single endpoint
- [x] Delete server-side RMP stuff
- [x] Change DB to not use ordinals, because they make it harder to do stuff like
      search the DB directly for stuff.

#### Delayed for now
- [ ] Make sure docker isn't doing something idiot for the exposed ports
- [ ] Optimize response times enough that `full` can be the default
- [ ] Profile stuffs, perf seems to be regressing randomly
- [ ] Use `schedge_meta` table to allow changing schools/subjects at runtime
- [ ] Remove all the different `models` folders, simplify database edits and whatnot
- [ ] Make an admin portal, including stuff like memory usage stats and whatnot
- [ ] Fuzzer? Some kind of tester I guess
- [ ] Upgrade to Java 17, use Java HttpClient (from JDK 11) to get rid of dependency
      on async-http-client

## Front Ends
If you'd like to write a front end to this API, you can list it below!

- https://courses.torchnyu.com (repo at https://github.com/NicholasLYang/courses )
- https://bobcatsearch.com (repo at https://github.com/EthanPrintz/bobcat-search )
- https://notalbert.netlify.com (repo at https://github.com/pecansalad/coursecatalog )
