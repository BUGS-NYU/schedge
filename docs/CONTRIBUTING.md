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
- PostgreSQl<sup>1</sup>
- Java
- Gradle

Some of this can be handled for you using the Intellij Idea IDE.

<small><sup>1</sup>You only need PostgreSQL to develop on portions of the project
that use the database; if you don't think you'll be doing that, then you dont need
to worry about it.</small>

<!-- @HelpWanted More detailed explanation of what to do for Intellij -->
### Using Intellij
[Install Intellij from the website][intellij-download], and then:
- From the welcome page, select **Check out from Version Control**
- Select Git from the drop down menu
- Use the URL for this repository ( https://github.com/A1Liu/schedge ).

You'll need to use the command line instructions to install PostgreSQL<sup>1<sup>,
but everything else should be handled for you.

[intellij-download]: https://www.jetbrains.com/idea/download/index.html

<small><sup>1</sup>You only need PostgreSQL to develop on portions of the project
that use the database; if you don't think you'll be doing that, then you dont need
to worry about it.</small>

### Note 
If you have `Java 14`, you MUST install the latest version of Gradle. Otherwise, 
it will not let you build

<!-- @HelpWanted Add command line installation instructions for more OSes -->
### Command Line: Linux
You can get more detailed instructions on each application's respective website.
However, TLDR versions are included below for you convenience.

#### Debian/Ubuntu
Type the following into the terminal:

```shell script
# Install Git
sudo apt install git-all

# Install PostgreSQL
sudo apt-get install postgres

# Install Java
sudo apt install openjdk-8-jdk openjdk-8-jre

# Install SDKMAN, a package manager that helps with gradle and kotlin
curl -s https://get.sdkman.io | bash

# Install Gradle
sdk install gradle 6.0.1
```

### Mac
If you are on Mac, installing HomeBrew, a package manager for MacOs, helps make
managing dependencies easier. Read the [documentation](https://docs.brew.sh/)
for more information, or just copy paste the script below:

```shell script
# Install homebrew
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

#Update homebrew if it's already installed on your machine
brew update
```

Git is a version control system and helps manage the project. Learn
[git](https://www.tutorialspoint.com/git/index.htm)

```shell script
#Install git.
brew install git
```

Postgresql is used as the database for Schedge

```shell script
# Install postgresql. postgresql is for the database
brew install postgresql
```

Java codes handle I/O and parsing the data. Read the
[Javadoc](https://docs.oracle.com/javase/7/docs/api/)

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

## Development

### Repository
- Fork this repository by clicking `fork` on the right side.
- Choose where you want to save the project locally on your computer then run
  `git clone [Your URL/Git here]`.
- We recommend to develop your feature through another branch other than the
  `master` branch. So, run `git checkout -b [name of your branch here]` and develop
  the feature. You can switch between branches by the same command by removing `-b`.
- Once you finish, you can run the normal `git add`, `git commit` & `git push`.
- Make a [PR](#user-content-pull-request) when the feature is fully finished or
  if you would like feedback on your changes.

<!-- @HelpWanted Make this more detailed -->
### Using IntelliJ
You'll need to specify the command line arguments to use when testing the app, but
in the typical case you can use the build button in the top right hand corner to
build/check your work, and the run button to run. The class `Main` is located in
Java folder.

### Using the Command Line
A [command-line interface (CLI)](https://en.wikipedia.org/wiki/Command-line_interface)
processes commands to a computer program in the form of lines of text. The program
which handles the interface is called a command-line interpreter or command-line
processor. This can easily be used through the Terminal (Mac) or Command Prompt
(Windows).  If you have taken Introduction to CS in Java, you are familiar with Scanner.
CLI is somewhat similar. It reads the inputs from the users but there are only some
subcommands defined in the project. We have provided them for Schedge that would quickly
allow you to query, scrape and parse Albert data. So shall we open the terminal and begin?

## Build
- `gradle build`: build the application.
- `java -jar .build/libs/schedge.jar`: test the code.
- `gradle checkFast`: quickly check if the code compiles.

## Issue
Remember to include enough information if you're reporting a bug.
Asking question through an issue is totally fine as well.

## Pull Request
It would be best to develop your feature with a new branch other than master.
Every PR will be considered.

### Before Creating a PR
- Making sure that the code compiles and test your code.
- Format your code with [clang-format](https://github.com/mprobst/ClangFormatIJ/).

## Comment Annotations
The codebase uses the following annotations in the comments:

- `@HelpWanted` - We need contributors for this code
- `@TODO` - We need to finish this code
- `@Performance` - This area can be tweaked/rewritten to improve performance
- `@CodeOrg` - We should reorganize this code

