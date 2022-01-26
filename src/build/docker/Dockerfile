FROM openjdk:8u212-jre-alpine3.9

EXPOSE 4358

COPY ./src/build/docker/entrypoint.sh /app/schedge
COPY ./.build/libs/schedge.jar /app/schedge.jar

ENV PATH "/app:$PATH"
RUN echo 'export PATH="/app:${PATH}"' >> /root/.bashrc

ENTRYPOINT ["/app/schedge", "db", "serve"]
