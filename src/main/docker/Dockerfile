FROM openjdk:11.0.13-jre

EXPOSE 4358
RUN mkdir /app

COPY ./.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["java", "-Xmx2G", "-Djdk.httpclient.allowRestrictedHeaders=host,connection", \
            "-jar", "/app/schedge.jar", "db", "serve"]
