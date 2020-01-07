FROM gradle AS build
COPY --chown=gradle:gradle . /home/gradle/schedge
WORKDIR /home/gradle/schedge
RUN gradle build --no-daemon

EXPOSE 8080

ENTRYPOINT ["./docker-entry.sh"]