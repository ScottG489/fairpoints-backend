FROM eclipse-temurin:17@sha256:9cb1a90ce9b7921fe79e544200b0ea1eab76943dd84fb284356ddb8c96e8f32a

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
