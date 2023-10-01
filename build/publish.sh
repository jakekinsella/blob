#! /bin/bash

export VERSION=$(git rev-parse --short HEAD)

echo $VERSION

export ARCH="arm64v8/"

mkdir -p tmp/build/docker/server
mkdir -p tmp/build/docker/notes

envsubst < build/docker/server/Dockerfile > tmp/build/docker/server/Dockerfile
envsubst < build/docker/notes/Dockerfile > tmp/build/docker/notes/Dockerfile

docker buildx build --platform linux/arm64/v8 . -f tmp/build/docker/server/Dockerfile -t "blob-arm:$VERSION"
docker buildx build --platform linux/arm64/v8 . -f tmp/build/docker/notes/Dockerfile -t "notes-arm:$VERSION"

aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com
docker tag $(docker images | grep blob-arm | head -n1 | awk '{print $3}') $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/blob:$VERSION
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/blob:$VERSION
docker tag $(docker images | grep notes-arm | head -n1 | awk '{print $3}') $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/notes:$VERSION
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/notes:$VERSION
