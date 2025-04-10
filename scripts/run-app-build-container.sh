#!/bin/sh
podman run -it -v  ./:/root/ -w=/root/ docker.io/maven:3.9.9-eclipse-temurin-23-alpine  mvn clean package; rm ./.bash_history
