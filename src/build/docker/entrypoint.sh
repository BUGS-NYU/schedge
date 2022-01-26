#!/bin/sh

# This makes it easier to run commands in the docker compose
java -Xmx1G -Djdk.httpclient.allowRestrictedHeaders=host,connection \
  -jar /app/schedge.jar $@
