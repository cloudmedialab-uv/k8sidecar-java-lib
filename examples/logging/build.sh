#!/bin/bash
mvn clean package

REPO="cloudmedialab/sidecar-logging"
TAG=1.0.0

docker build target -t sidecar-logging:$TAG -f docker/Dockerfile
docker tag sidecar-logging:$TAG $REPO:$TAG
docker push $REPO:$TAG
mvn clean
