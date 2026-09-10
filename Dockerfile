FROM eclipse-temurin:17@sha256:36d9a76dc231587873b103c68a789b85d91b41d314dda69730d6bc43a777f2a9

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
