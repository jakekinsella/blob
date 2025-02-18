FROM common_ocaml

WORKDIR /home/opam/server

ADD blob/blob.opam .
RUN opam pin add -yn blob .

RUN opam --cli=2.1 list --readonly --external --no-depexts --resolve=blob --color=never | tee depexts

RUN opam install -y --deps-only --no-depexts blob .

ADD blob .
RUN rm -rf _build/
RUN opam exec dune build

FROM alpine:3.18
WORKDIR /app

COPY --from=0 /home/opam/server/_build/default/bin/ .

COPY --from=0 /home/opam/server/depexts depexts
RUN cat depexts | xargs apk --update add && rm -rf /var/cache/apk/*

EXPOSE 8080
ENV PRODUCTION=1
CMD ./server/main.exe
