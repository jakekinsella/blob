# Blob

Simple blob storage + notes frontend.

## Local Development

Barebones, totally local development environment.  

### Dependencies
 - [central](https://github.com/TheLocust3/central) cloned + running locally
 - opam
   - `brew install opam`
 - [dune](https://dune.build)
 - [yarn](https://yarnpkg.com)
 - postgres
 - libpq
   - `brew install libpq`
 - openssl
   - `brew install openssl`

### Initial Setup
Complete the prerequisites found at [Central](https://github.com/TheLocust3/central?tab=readme-ov-file#initial-setup).  
  
Create `database.env` at the root of the repository:
```
PGUSER=jakekinsella
PGPASSWORD=
PGHOST=localhost
PGPORT=5432
PGDATABASE=blob
```
  
`createdb blob`  
`make install`  
`cd blob && make migrate`  

### Run
`cd blob && make start`  
`cd notes && make start`
  
Navigate to `http://localhost:8280`  

## Local Deploy
Complete the prerequisites found at [Central](https://github.com/TheLocust3/central?tab=readme-ov-file#local-deploy).  
  
`eval $(minikube docker-env)`  
`sudo sh -c 'echo "127.0.0.1       blob.cluster.local" >> /etc/hosts'`
`sudo sh -c 'echo "127.0.0.1       notes.cluster.local" >> /etc/hosts'`
  

#### Build+Deploy
`eval $(minikube docker-env)`
`make local-publish`  
`make local-deploy`  
  
Navigate to `https://notes.cluster.local/login`  

## Cloud Deploy
Complete the prerequisites found at [Central](https://github.com/TheLocust3/central?tab=readme-ov-file#cloud-deploy).  
  
Environment variables:
```
export AWS_ACCESS_KEY_ID=???
export AWS_SECRET_ACCESS_KEY=???
export AWS_ACCOUNT_ID=???
export AWS_DEFAULT_REGION=us-east-1
```
  
Initialize the build depedencies:  
`make aws-init`

### AWS Setup
Set up the ECR repo:  
`make aws-repo`

### Cluster Deploy

Export the Control Plane IP:  
`export CONTROL_PLANE_IP=???`  
`export NODE_IP=???`  

Deploy the cluster:  
`make cluster-publish`  
`make cluster-deploy VERSION=???`  

### To-Do
 - Add link sharing for blob/notes
