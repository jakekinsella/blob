FROM common_ocaml

WORKDIR /home/opam/server

ADD blob/blob.opam .
RUN opam pin add -yn blob . && \
    opam depext blob && \
    opam install --deps-only blob

ADD blob .
RUN sudo chown -R opam:nogroup . && \
    opam config exec make build && \
    opam depext -ln blob > depexts

FROM alpine
WORKDIR /app
COPY --from=0 /home/opam/server/_build/default/bin/ .

COPY --from=0 /home/opam/server/depexts depexts
RUN cat depexts | xargs apk --update add && rm -rf /var/cache/apk/*

EXPOSE 8080
ENV PRODUCTION=1
CMD ./server/main.exe
