FROM openjdk:11.0.13-jre

EXPOSE 4358

COPY ./src/build/docker/entrypoint.sh /app/schedge
COPY ./.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["/app/schedge", "db", "serve"]
