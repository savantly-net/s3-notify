#!/bin/bash


set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR

./mvnw package -Dquarkus.package.type=uber-jar

docker build -t savantly/s3notify:latest .