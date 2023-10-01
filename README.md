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
`initdb data`  
`pg_ctl -D data -l logfile start`  
`createdb blob`  
`make install`  
`cd blob && make migrate`  
  
Create `database.env` at the root of the repository:
```
PGUSER=jakekinsella
PGPASSWORD=
PGHOST=localhost
PGPORT=5432
PGDATABASE=blob
```

### Run
`cd blob && make start`  
`cd notes && make start`
  
Navigate to `http://localhost:8280`  

## Local Deploy
Deployed as a Kubernetes cluster.  

### Dependencies
 - [Docker Desktop](https://www.docker.com/products/docker-desktop/)
 - [minikube](https://minikube.sigs.k8s.io/docs/)
 - [dune](https://dune.build)
 - [yarn](https://yarnpkg.com)

#### Initial Setup

`minikube start`  
`eval $(minikube docker-env)`  
`minikube addons enable ingress`  
`minikube tunnel`  
`sudo sh -c 'echo "127.0.0.1       blob.localhost" >> /etc/hosts'`
`sudo sh -c 'echo "127.0.0.1       notes.localhost" >> /etc/hosts'`
  

#### Build+Deploy
`make local-publish`  
`make local-deploy`  

... some amount of waiting ...  
`kubectl get pods` should show the containers starting up  
  
Navigate to `https://notes.localhost/login`  

## Cloud Deploy
Deploy a single node Kubernetes cluster in AWS.  

### Dependencies
 - [Packer](http://packer.io)
 - [Terraform](https://www.terraform.io)

### Initial Setup
  
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

... wait \~10minutes time (until `sudo kubectl get pods` shows all the containers running) ...  

## TODO
 - notes ui apple pencil support
