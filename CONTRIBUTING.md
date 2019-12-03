# Contributing

## Setup
You'll need to install a few applications to contribute to this project:

- Git
- Postgres<sup>1</sup>
- Java/Kotlin
- Gradle

Some of this can be handled for you using the Intellij Idea IDE.

<small><sup>1</sup>You only need PostGres to develop on portions of the project
that use the database; if you don't think you'll be doing that, then you dont need
to worry about it.</small>

<!-- @HelpWanted More detailed explanation of what to do for Intellij -->
### Using Intellij
[Install Intellij from the website][intellij-download], and then from the welcome
page, select **Check out from Version Control**, select Git from the drop
down menu, then use the URL for this repository
( https://github.com/NicholasLYang/schedge ). You'll need to use the command
line instructions to install Postgres<sup>1<sup>, but everything else should be
handled for you.

[intellij-download]: https://www.jetbrains.com/idea/download/index.html

<small><sup>1</sup>You only need PostGres to develop on portions of the project
that use the database; if you don't think you'll be doing that, then you dont need
to worry about it.</small>

<!-- @HelpWanted Add command line installation instructions for more OSes -->
### Command Line: Linux
You can get more detailed instructions on each application's respective website.
However, TLDR versions are included below for you convenience.

#### Debian/Ubuntu
Type the following into the terminal:

```shell script
# Install Git
sudo apt install git-all

# Install Postgres
sudo sh -c 'echo \
  "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main"
  >> /etc/apt/sources.list.d/pgdg.list'
wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
sudo apt-get update
sudo apt-get install postgresql-10

# Install Java
sudo apt install openjdk-8-jdk openjdk-8-jre

# Install SDKMAN, a package manager that helps with gradle and kotlin
curl -s https://get.sdkman.io | bash

# Install Gradle
sdk install gradle 6.0.1

# Install Kotlin
sdk install kotlin
```

### Mac
If you are on Mac, installing homebrew helps manage dependencies easier for your life.
Homebrew is a package manager for MacOS, helping installation of other dependencies become easier. 
Read the [documentation](https://docs.brew.sh/)
```shell script
# Install homebrew
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

#Update homebrew if it's already installed on your machine
brew update
```
Git is a version control system and helps manage the project. Learn [git](https://www.tutorialspoint.com/git/index.htm)
```shell script
#Install git.
brew install git 
```
Postgresql is used as the database for Schedge. Read more about it [here](https://www.postgresql.org/about/)
```shell script
# Install postgresql. postgresql is for the database
brew install postgresql
``` 
Java codes handle I/O and parsing the data. Read the [Javadoc](https://docs.oracle.com/javase/7/docs/api/)
```shell script
#Install Java.
brew cask install java
```
```shell script
# Install SDKMAN, a package manager that helps with gradle and kotlin
curl -s https://get.sdkman.io | bash
```
Gradle is a build tool to manage dependencies. Read the [documentation](https://docs.gradle.org/current/userguide/what_is_gradle.html)
```shell script
# Install Gradle. Gradle is the build tools for schedge
brew install gradle
```
Kotlin code handles scraping and querying data. Read the [documentation](https://kotlinlang.org/docs/reference/)
```shell script
# Install Kotlin. 
brew install kotlin
```

## Development

<!-- @HelpWanted Make this more detailed -->
### Using IntelliJ
You'll need to specify the command line arguments to use when testing the app, but
in the typical case you can use the build button in the top right hand corner to
build/check your work, and the run button to run.

### Using the Command Line
Use `gradle build` to build the application, then test using
`java -jar .build/libs/schedge-all.jar`. Since `gradle build` takes a while, you
should use `gradle checkFast` to ensure your code compiles, then run `gradle build`
when you're ready to test your code.

## Code organization
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

## Comment Annotations
The codebase uses the following annotations in the comments:

- `@HelpWanted` - We need contributors for this code
- `@TODO` - We need to finish this code
