# FROM openjdk:8u212-jre-alpine3.9
FROM openjdk:17.0.2-jdk-slim-buster

WORKDIR "/home/user"

EXPOSE 4358

ENV PATH "/app:$PATH"
RUN echo 'export PATH="/app:${PATH}"' >> /root/.bashrc

COPY ./src/build/docker/entrypoint.sh /app/schedge
COPY ./.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["/app/schedge", "serve"]
