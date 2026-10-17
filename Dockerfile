FROM eclipse-temurin:17@sha256:fe947d993c32dd9cf651a4353a6ed723c2370b3a9d034ea4cab4775404ea531a

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
