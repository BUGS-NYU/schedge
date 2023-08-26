# @Note: Can use the following to prevent Docker's "load metadata"
# bullshit: https://stackoverflow.com/a/70483395
#
# TLDR:
#
#   ```
#   # Do this BEFORE losing internet connection
#   docker pull eclipse-temurin:19-jre-alpine
#   docker tag eclipse-temurin:19-jre-alpine schedge/base
#
#   # in Dockerfile, edit the line below to be:
#   FROM schedge/base
#   ```
#
#                               - Albert Liu, Nov 06, 2022 Sun 13:21
FROM eclipse-temurin:19-jre-alpine

WORKDIR "/home/user"

EXPOSE 4358

ENV PATH "/app:$PATH"
RUN echo 'export PATH="/app:${PATH}"' >> /root/.bashrc

COPY ./src/build/entrypoint.sh /app/schedge

# This is not strictly necessary, but alerts the developer to problems with the
# executable before they happen in the container; the last time there was
# an issue here:
#
# - If the issue is CRLF stuffs: I have absolutely no idea how to actually fix it,
#   but using `git config core.autocrlf false` at least fixed it for me.
#
#                                     - Albert Liu, Oct 09, 2022 Sun 17:54 EDT
RUN chmod +x /app/schedge

COPY ./build/libs/schedge.jar /app/schedge.jar

ENTRYPOINT ["/app/schedge", "serve"]
