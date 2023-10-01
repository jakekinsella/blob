#! /bin/bash

export IMAGE="blob:latest"
export NOTES_IMAGE="notes:latest"
export IMAGE_POLICY="Never"
export POSTGRES_LB="---
apiVersion: v1
kind: Service
metadata:
  name: postgres
spec:
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
  type: LoadBalancer"
export HOST="blob.localhost"
export UI_HOST="notes.localhost"
export CENTRAL_BASE="http://central-server:8080/api"

for f in build/cluster/*.yaml; do envsubst < $f | kubectl apply -f -; done
