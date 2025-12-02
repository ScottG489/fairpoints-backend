FROM eclipse-temurin:11@sha256:189ce1c8831fa5bdd801127dad99f68a17615f81f4aa839b1a4aae693261929a

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
