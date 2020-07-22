FROM openjdk:11

RUN mkdir /opt/app
COPY build/install/service/ /opt/app
CMD ["/opt/app/bin/start-server", "server"]
