publish:
	mkdir -p tmp/build/docker/server
	mkdir -p tmp/build/docker/notes
	
	envsubst < build/docker/server/Dockerfile > tmp/build/docker/server/Dockerfile
	envsubst < build/docker/notes/Dockerfile > tmp/build/docker/notes/Dockerfile


	docker image build . -f tmp/build/docker/server/Dockerfile -t blob:latest --no-cache

# 	make -f notes/Makefile release
# 	docker image build . -f tmp/build/docker/ui/Dockerfile -t notes:latest

deploy:
	./build/local/deploy.sh

teardown:
	./build/local/teardown.sh
