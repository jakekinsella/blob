publish:
	mkdir -p tmp/build/docker/server
	mkdir -p tmp/build/docker/notes
	
	envsubst < build/docker/server/Dockerfile > tmp/build/docker/server/Dockerfile
	envsubst < build/docker/notes/Dockerfile > tmp/build/docker/notes/Dockerfile


	docker buildx build . -f tmp/build/docker/server/Dockerfile -t blob:latest

	cd notes && make release
	cd ..
	docker buildx build . -f tmp/build/docker/notes/Dockerfile -t notes:latest

deploy:
	./build/local/deploy.sh

teardown:
	./build/local/teardown.sh
