#!/bin/bash

for i in 1 2 3 4 5; do gradle updateDb --no-daemon && break || sleep 15; done
java -jar /home/gradle/src/.build/libs/schedge-all.jar db scrape --term 1204
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
-Djava.security.egd=file:/dev/./urandom  -jar /home/gradle/src/.build/libs/schedge-all.jar db serve
