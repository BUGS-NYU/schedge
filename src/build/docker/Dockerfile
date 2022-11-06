# FROM openjdk:8u212-jre-alpine3.9

# @Note: Can use the following to prevent Docker's "load metadata"
# bullshit: https://stackoverflow.com/a/70483395
#
# TLDR is that you need to download the image ahead of time, and then
# tag it with a custom name (e.g. schedge/jdk-slim) and use the new
# custom name in the Dockerfile (i.e. FROM schedge/jdk-slim).
#
#                               - Albert Liu, Nov 06, 2022 Sun 13:21
FROM openjdk:17.0.2-jdk-slim-buster

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
