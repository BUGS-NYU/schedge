FROM gradle:7.3.3-jdk11-alpine AS build
WORKDIR /home/gradle/schedge

COPY --chown=gradle:gradle ./build.gradle ./build.gradle

# This run takes 20 seconds, the next one also takes around 20 seconds when this
# is uncommented. Without this line it takes 27 seconds.
RUN gradle build --no-daemon

COPY --chown=gradle:gradle ./src/ ./src/
RUN gradle build --no-daemon

FROM openjdk:11.0.13-jre

EXPOSE 4358

RUN mkdir /app

COPY --from=build /home/gradle/schedge/.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["java", "-Xmx2G", "-Djdk.httpclient.allowRestrictedHeaders=host,connection", \
            "-jar", "/app/schedge.jar", "db", "serve"]
