#! /bin/bash

export VERSION=${1:-latest}

echo "Pushing to ${CONTROL_PLANE_IP} @ $VERSION"

export IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/blob:$VERSION"
export NOTES_IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/note:$VERSION"
export IMAGE_POLICY="IfNotPresent"
export HOST="blob.jakekinsella.com"
export UI_HOST="notes.jakekinsella.com"
export CENTRAL_BASE="http://central-server:8080/api"

rm -rf tmp/
mkdir -p tmp/build/cluster/

for f in build/cluster/*.yaml; do envsubst < $f > tmp/$f; done

ssh ubuntu@"${CONTROL_PLANE_IP}" "mkdir -p ~/cluster/blob/"

scp -r secrets.env ubuntu@"${CONTROL_PLANE_IP}":~/secrets.env
scp -r tmp/build/cluster/* ubuntu@"${CONTROL_PLANE_IP}":~/cluster/blob/
