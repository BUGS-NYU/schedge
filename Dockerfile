FROM gradle:jdk13 AS build

RUN mkdir /home/gradle/schedge
WORKDIR /home/gradle/schedge


COPY --chown=gradle:gradle src src
COPY --chown=gradle:gradle build.gradle build.gradle
RUN gradle build --no-daemon

COPY --chown=gradle:gradle docker-entry.sh docker-entry.sh
COPY --chown=gradle:gradle schedge schedge

EXPOSE 8080

ENTRYPOINT ["./docker-entry.sh"]