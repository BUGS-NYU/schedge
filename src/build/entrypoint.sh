#!/bin/sh

# This script makes it easier to run commands in the docker compose
exec java -Xmx1G -Djdk.httpclient.allowRestrictedHeaders=host,connection \
  -jar /app/schedge.jar "$@"
