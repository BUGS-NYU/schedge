FROM gradle AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

EXPOSE 8080

ENTRYPOINT ["./docker-entry.sh"]