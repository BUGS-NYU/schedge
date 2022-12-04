# FROM openjdk:8u212-jre-alpine3.9

# @Note: Can use the following to prevent Docker's "load metadata"
# bullshit: https://stackoverflow.com/a/70483395
#
# TLDR:
#
#   ```
#   # Do this BEFORE losing internet connection
#   docker pull openjdk:17.0.2-jdk-slim-buster
#   docker tag openjdk:17.0.2-jdk-slim-buster schedge/base
#
#   # in Dockerfile
#   FROM schedge/base
#   ```
#
#                               - Albert Liu, Nov 06, 2022 Sun 13:21
# FROM openjdk:17.0.2-jdk-slim-buster
# Example of custom Java runtime using jlink in a multi-stage container build
FROM eclipse-temurin:11-alpine as jre-build

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
        --add-modules java.se \
        --add-modules jdk.zipfs \
        --strip-debug \
        --no-man-pages \
        --no-header-files \
        --compress=2 \
        --output /javaruntime

# Define your base image
FROM alpine:3.17
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

# Continue with your application deployment
WORKDIR "/home/user"

EXPOSE 4358

ENV PATH "/app:$PATH"
RUN echo 'export PATH="/app:${PATH}"' >> /root/.bashrc

COPY ./src/build/docker/entrypoint.sh /app/schedge

# This is not strictly necessary, but alerts the developer to problems with the
# executable before they happen in the container; the last time there was
# an issue here:
#
# - If the issue is CRLF stuffs: I have absolutely no idea how to actually fix it,
#   but using `git config core.autocrlf false` at least fixed it for me.
#
#                                     - Albert Liu, Oct 09, 2022 Sun 17:54 EDT
RUN chmod +x /app/schedge

COPY ./.build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["/app/schedge", "serve"]
