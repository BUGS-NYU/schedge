# Developing on Mac
You'll need the following programs to work on Schedge:

- Git
- Postgres
- Java

## Setting up your Computer for Development
You'll need to install developer tools in order to use Git; that means opening
Terminal and running the following command:

```
xcode-select --install
```

## Installing Postgres Using Homebrew
The easiest way to install Postgres is using HomeBrew, a package manager 
for MacOs. To install HomeBrew, run the following command

```
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

Then, to install Postgres, run

```
brew install postgresql
```

With Postgres installed, you'll need to start the server, which you can do using

```
pg_ctl -D /usr/local/var/postgres start
```

Then, you need to create the Schedge user and database, which can be done using

```
createuser -sl schedge
createdb schedge
```

Finally, you need to generate DB code using

```
./gradlew updateDb
```

To compile the project, you can then run

```
./gradlew build
```

and then run the schedge server locally using

```
./schedge db serve
```
