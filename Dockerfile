FROM eclipse-temurin:11@sha256:8396f3215a7f8a10371e107c073cd74b0600532171c4683eebc8b1a0491381cc

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
