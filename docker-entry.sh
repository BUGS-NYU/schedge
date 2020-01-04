#!/bin/bash

java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
-Djava.security.egd=file:/dev/./urandom  -jar /home/gradle/schedge/.build/libs/schedge-all.jar db serve
