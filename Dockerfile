FROM eclipse-temurin:17@sha256:bc033b57e11b773c3043babfd664e7a5ef110805548b921cbfc3e8c67a0725d6

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
COPY config.yml /opt/app/config.yml
CMD ["/opt/app/bin/start-server", "server", "/opt/app/config.yml"]
