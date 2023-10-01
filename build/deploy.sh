#! /bin/bash

export VERSION=${1:-latest}

echo "Deploying to ${CONTROL_PLANE_IP} @ $VERSION"

./build/push.sh $VERSION

ssh ubuntu@"${CONTROL_PLANE_IP}" "sudo ./ecr_refresh.sh"
ssh ubuntu@"${NODE_IP}" "sudo ./ecr_refresh.sh"
ssh ubuntu@"${CONTROL_PLANE_IP}" "sudo kubectl apply -f ~/cluster/blob/"
