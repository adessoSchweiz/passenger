#!/usr/bin/env bash

cd $(dirname $0)

VERSION=1.0.0
echo VERSION: $VERSION

mvn clean install
docker build -t adesso/passenger:${VERSION} .
docker stop passenger
docker rm passenger

docker run -d \
   --name passenger \
   --net=hackathon \
   -p 8091:8080 \
   -e BOOTSTRAP_SERVERS=kafka-1:29092  \
   -e SCHEMA_REGISTRY_URL=http://schema-registry:8081 \
   -e APPLICATION_SERVER=localhost:8091 \
   adesso/passenger:${VERSION}
