FROM eclipse-temurin:17@sha256:91011320978c10ec51d2cde1eb6f616db6d2a739a0e521096c343dd3fed9abe6

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
