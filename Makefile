install:
	cd blob && make install
	cd notes && make install

build:
	cd blob && make build

clean:
	cd blob && make clean

local-publish:
	make -f build/local/Makefile publish

local-deploy:
	make -f build/local/Makefile deploy

local-teardown:
	make -f build/local/Makefile teardown

cluster-publish:
	make -f build/Makefile publish

cluster-deploy:
	make -f build/Makefile deploy VERSION=$(VERSION)

cluster-teardown:
	make -f build/Makefile teardown

aws-init:
	cd build/aws && make init

aws-repo:
	cd build/aws && make repository
