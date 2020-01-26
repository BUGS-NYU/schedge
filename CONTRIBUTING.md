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
[Install Intellij from the website][intellij-download], and then:
- From the welcome page, select **Check out from Version Control**
- Select Git from the drop down menu
- Use the URL for this repository ( https://github.com/A1Liu/schedge ).

You'll need to use the command line instructions to install Postgres<sup>1<sup>,
but everything else should be handled for you.

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
If you are on Mac, installing HomeBrew, a package manager for MacOs, helps make
managing dependencies easier. Read the [documentation](https://docs.brew.sh/) for more information,
or just copy paste the script below:

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
## Project Overview
The following ASCII file structure diagram shows the most important packages and files, with comments.
``` bash
    .
    ├── build.gradle                                    // Gradle build tool to get dependencies, build automation
    └── src
        └── main
            ├── java                                    // Java code currently being refactored from Kotlin. Handle I/O, database, and CLI
            │   ├── Main.java                           // Main to run cli  
            │   ├── api                                 // Handling API endpoints and documentation
            │   │   ├── App.java                        // Main and abstract classes of the API
            │   │   ├── CoursesEndpoint.java            // Courses endpoint providing catalog's data 
            │   │   ├── SchoolsEndpoint.java            // Schools endpoint providing list of schools
            │   │   ├── SubjectsEndpoint.java           // Subjects endpoint providing list of subjects available
            │   │   └── models                          // Data objects for the API
            │   │       ├── Course.java                 
            │   │       ├── Meeting.java
            │   │       ├── School.java
            │   │       ├── Section.java
            │   │       └── Subject.java
            │   ├── cli                                 // cli - command-line interface
            │   │   ├── parse.java                      // Parse commands based on input file or console 
            │   │   ├── query.java                      // Query commands for catalog, sections and schools/subjects
            │   │   ├── schedge.java                    // Schedge commands to provide all subcommands: query, parse and scrape
            │   │   └── scrape.java                     // Scrape commands for catalog, sections and schools/subjects
            │   ├── scraping
            │   │   ├── SimpleBatchedFutureEngine.java  // Handling asynchronous scraping with Future
            │   │   └── models                          // Data objects for scraping 
            │   │       ├── CatalogData.java            
            │   │       ├── CatalogQueryData.java
            │   │       ├── Course.java
            │   │       ├── Meeting.java
            │   │       ├── School.java
            │   │       ├── Section.java
            │   │       ├── SectionAttribute.java
            │   │       └── Subject.java
            │   └── services                            // Parsing, Inserting and Connecting to Postgres database
            │       ├── GetConnection.java
            │       ├── InsertCourses.java
            │       ├── JsonMapper.java
            │       ├── ParseCatalog.java
            │       ├── ParseSchoolSubjects.java
            │       ├── ParseSection.java
            │       └── SelectCourses.java
            ├── kotlin                                  // Handling scraping for now. Currently being refactored into Java
            │   ├── models                              // Data objects for query and scraping
            │   │   └── nyu.kt
            │   ├── services                            // The meat of the scraping: query & scrape catalogs, sections and subjects asynchronously
            │   │   ├── query_catalog.kt                // queries the NYU Albert course catalog                                                                                                              
            │   │   ├── query_school.kt                 // queries NYU Albert for schools/subjects based on term
            │   │   ├── query_section.kt                // queries NYU Albert for section descriptions
            │   │   ├── scrape_catalog.kt               // scrapes the NYU Albert course catalog
            │   │   └── scrape_section.kt               // scrapes the NYU Albert course section descriptions
            └── resources                               // Schools and subjects stored in txt file for now
                ├── schools.txt
                └── subjects.txt
```

## Development

### Repository
- Fork this repository by clicking `fork` on the right side. 
- Choose where you want to save the project locally on your computer then run `git clone [Your URL/Git here]`.
- We recommend to develop your feature through another branch other than the `master` branch. 
So, run `git checkout -b [name of your branch here]` and develop the feature. You can switch between
branches by the same command by removing `-b`. 
- Once you finish, you can run the normal `git add`, `git commit` & `git push`. 
- Make a [PR](#user-content-pull-request) when the feature is fully finished or if you would like
feedback on your changes. 

<!-- @HelpWanted Make this more detailed -->
### Using IntelliJ
You'll need to specify the command line arguments to use when testing the app, but
in the typical case you can use the build button in the top right hand corner to
build/check your work, and the run button to run. The class `Main` is located in Java folder. 

### Using the Command Line

#### What is it you ask ?!!
A [command-line interface (CLI)](https://en.wikipedia.org/wiki/Command-line_interface) processes commands to a computer program in the form of 
lines of text. The program which handles the interface is called a command-line interpreter 
or command-line processor. This can easily be used through the Terminal (Mac) or Command Prompt (Windows).
If you have taken Introduction to CS in Java, you are familiar with Scanner. CLI is somewhat similar. It reads the inputs 
from the users but there are only some subcommands defined in the project. We have provided them for Schedge that 
would quickly allow you to query, scrape and parse Albert data. So shall we open the terminal and begin? 

## Build
- `gradle build`: build the application.
- `java-jar .build/libs/schedge-all.jar`: test the code. 
- `gradle checkFast`: quickly check if the code compiles. 

## Commands
The following commands are valid in Schedge:

```sh
./schedge query catalog     // Query the catalog
./schedge query sections    // Query a section
./schedge query school      // Query a school/subject
./schedge parse catalog     // Parse the catalog
./schedge parse section     // Parse the section
./schedge parse school      // Parse the school/subject
./schedge scrape catalog    // scrape the catalog
./schedge scrape sections   // scrape the sections
./schedge scrape school     // scrape the school/subject

// In development
./schedge db scrape // scrape the catalog for information
./schedge db query  // query the database for information
./schedge db serve  // Serve the database data through an API
```

#### Parameters
These are generally true for most commands. For detailed parameters of each specific commands,
we type, for instance, `./schedge query`. CLI will then prompt us the information for the other 
parameters and its descriptions. Please read it carefully to input the correct format. 
The order of the parameters do not matter as long as the parameters' names and formats are correct.

```sh
--term                  // term = (year - 1900) * 10 + (2,4,6 OR 8). Shortcut version. Recommend to use year and semester instead             
--year                  // integer 
--semester              // string. sp, ja, fa, OR su
--registration-number   // the number for each catalog. Can be found on Albert mobile more specifically
--subject               // subject code: CSCI, MA, ECON, etc
--school                // school code: UA, UY, UT, UB, etc
--batch-size            // batch size to do async services. 
``` 

#### Example 
```sh
./schedge scrape catalog --year 2020 --term sp --school UA --subject CSCI 
```
The above command will scrape catalog data from Albert website for Computer Science courses at the 
College of Arts and Sciences for the Spring, 2020. 

## Comment Annotations
The codebase uses the following annotations in the comments:

- `@HelpWanted` - We need contributors for this code
- `@TODO` - We need to finish this code
- `@Performance` - This area can be tweaked/rewritten to improve performance
- `@CodeOrg` - We should reorganize this code

## Issue
Remember to include enough information if you're reporting a bug.
Asking question through an issue is totally fine as well. 

## Pull Request
It would be best to develop your feature with a new branch other than master.
Every PR will be considered.

### Before Creating a PR
- Making sure that the code compiles and 
test your code.
- Format your code with [clang-format](https://github.com/mprobst/ClangFormatIJ/).    

