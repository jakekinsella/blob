FROM common_react

WORKDIR /ui

ADD notes/resources/public/ public/

FROM nginx
COPY --from=0 /ui/public/ /usr/share/nginx/html/
COPY build/docker/ui/default.conf /etc/nginx/conf.d/default.conf

ENV PRODUCTION=1
