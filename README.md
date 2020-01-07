# Schedge
Schedge is an open source API to NYU's course catalog, geared at eventually helping
NYU students plan their courses easier and faster.

NYU Albert is really annoying to use, and so is the rest of NYU's course catalog
infrastructure. Our goal with this API is to make it easier for students to plan
out their schedules, and eventually to also do some of that for them.

API Docs: https://schedge.torchnyu.com

## CLI Interface
The following commands are valid in Schedge:

```sh
./schedge query catalog # Query the catalog
./schedge query section # Query a section
./schedge parse catalog # Parse the catalog
./schedge scrape catalog # scrape the catalog
./schedge db scrape # scrape the catalog for information
./schedge db query # query the database for information
./schedge db serve # Serve the database data through an API
```

For available flags/subcommands for each command, use the `--help` flag. For example,
`./schedge query --help` will list the two subcommands `catalog` and `section`,
and `./schedge query section --help` will list the flags `--term`, `registrationNumber`,
and `output-file`.

## API Documentation
The API documentation is available at https://schedge.torchnyu.com .

## Contributing
If you'd like to contribute, please take a look at out [contributing guide](CONTRIBUTING.md).

## Front Ends
If you'd like to write a front end to this API, you can list it below!

- https://courses.torchnyu.com
