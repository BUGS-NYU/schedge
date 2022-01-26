# Docker Commands

```
docker-compose -f src/build/docker/production.docker-compose.yml up -d

# Run this to set up the servers for the first time
docker-compose up -d

# Build and upload a new version of Schedge to the server
./gradlew build
docker-compose build

docker-compose down
```
