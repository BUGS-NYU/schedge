# FROM openjdk:8u212-jre-alpine3.9
FROM openjdk:17.0.2-jdk-slim-buster

WORKDIR "/home/user"

EXPOSE 4358

ENV PATH "/app:$PATH"
RUN echo 'export PATH="/app:${PATH}"' >> /root/.bashrc

COPY ./src/build/docker/entrypoint.sh /app/schedge
COPY ./.build/libs/schedge.jar /app/schedge.jar

# This is not strictly necessary, but alerts the developer to problems with the
# executable before they happen in the container; the last time there was
# an issue here:
#
# - If the issue is CRLF stuffs: I have absolutely no idea how to actually fix it,
#   but using `git config core.autocrlf false` at least fixed it for me.
#
#                                     - Albert Liu, Oct 09, 2022 Sun 17:54 EDT
RUN chmod +x /app/schedge

ENTRYPOINT ["/app/schedge", "serve"]
