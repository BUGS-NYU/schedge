FROM openjdk:8u212-jre-alpine3.9

EXPOSE 4358

COPY ./src/build/docker/entrypoint.sh /app/schedge
COPY ./.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["/app/schedge", "db", "serve"]
