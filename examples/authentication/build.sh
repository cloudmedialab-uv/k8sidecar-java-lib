#!/bin/bash
mvn clean package

REPO="cloudmedialab/sidecar-authentication"
TAG=1.0.0

docker build target -t sidecar-authentication:$TAG -f docker/Dockerfile
docker tag sidecar-authentication:$TAG $REPO:$TAG
docker push $REPO:$TAG
mvn clean
