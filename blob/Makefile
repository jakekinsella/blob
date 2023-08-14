install:
	export C_INCLUDE_PATH=`ocamlc -where`:$C_INCLUDE_PATH
	export PKG_CONFIG_PATH=/usr/local/opt/zlib/lib/pkgconfig:/usr/local/opt/openssl/lib/pkgconfig
	export LDFLAGS="-L/usr/local/opt/libpq/lib"
	export CPPFLAGS="-I/usr/local/opt/libpq/include"
	opam pin add -yn blob .
	opam install --deps-only blob

build:
	dune build

start: build
	export `cat ../database.env` && PORT=2002 CENTRAL_BASE="http://localhost:2001/api" dune exec blob

rollback: build
	export `cat ../database.env` && dune exec database rollback

migrate: build
	export `cat ../database.env` && dune exec database migrate

clean:
	rm -rf _build

.PHONY: test
test:
	dune test
