FROM gradle AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", \
"-Djava.security.egd=file:/dev/./urandom",  "-jar", "/home/gradle/src/.build/libs/schedge-all.jar", "db", "serve"]