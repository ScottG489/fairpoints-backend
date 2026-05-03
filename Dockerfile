FROM eclipse-temurin:17@sha256:aae0b1494a5637b2c1b933080088ccc196dec7ffb83ce1cd524211ea4f640ff4

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
