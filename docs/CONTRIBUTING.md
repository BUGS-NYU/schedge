# Contributing

#### Table of Contents
- [Setup](#user-content-setup)
- [Development](#user-content-development)
- [Project Overview](#user-content-project-overview)
- [Comment Annotations](#user-content-comment-annotations)
- [Issue](#user-content-issue)
- [Pull Request](#user-content-pull-request)

## Setup
You'll need to install a few applications to contribute to this project:

- [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git) -
  This is used for version control
- [Git Large File Storage (LFS)](https://docs.github.com/en/repositories/working-with-files/managing-large-files/installing-git-large-file-storage) -
  This is used to store the Gradle Wrapper JAR without using up too much space.
- [Docker](https://docs.docker.com/get-docker/) -
  This is used to run the application in both production and development. Ideally
  it would not be necessary, but for now it is, to reduce the need to download
  an infinite number of dependencies all the time.
- [Java 8 or later](https://www.java.com/en/download/manual.jsp) -
  The project is written in Java, so you'll probably need to install a Java
  Development Kit (JDK) in order to build and run it.

## Development

### Repository
- Fork this repository by clicking `fork` on the right side.
- Choose where you want to save the project locally on your computer then run
  `git clone [Your URL/Git here]`.
- We recommend to develop your feature through another branch other than the
  `main` branch. So, run `git checkout -b [name of your branch here]` and develop
  the feature. You can switch between branches by the same command by removing `-b`.
- Once you finish, you can run the normal `git add`, `git commit` & `git push`.
- Make a [PR](#user-content-pull-request) when the feature is fully finished or
  if you would like feedback on your changes.

### Build with Command Line/Terminal
- `./gradlew check`: Check the application for compile-time errors
- `./gradlew composeUp`: Start up the development servers; Schedge will start
  on port `4358`
- `./gradlew composeBuild`: Build the application, and update the development server
- `./gradlew build`: Just build the application
- `docker-compose build`: Build the Docker image (the dev server will restart with
  new changes automatically)
- `docker-compose down`: Close the servers when development is done

### Comment Annotations
The codebase uses the following annotations in the comments:

- `@Help` - Help wanted
- `@TODO` - Code has something that's yet to be done
- `@Note` - A note for readers
- `@Performance` - This area can be tweaked/rewritten to improve performance
- `@Organize` - We should reorganize this code

### Before Creating a PR
- Making sure that the code compiles and test your code.
- Format your code with [clang-format](https://github.com/mprobst/ClangFormatIJ/).

