#!/bin/bash

java -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom \
  -jar /home/gradle/schedge/.build/libs/schedge.jar db serve
