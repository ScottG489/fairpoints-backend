FROM ubuntu:20.04

RUN apt-get update && \
  DEBIAN_FRONTEND=noninteractive apt-get install -y \
  git \
  docker.io \
  openjdk-17-jdk \
  jq \
  ansible \
  curl \
  unzip

ARG TERRAFORM_VERSION=0.12.26
RUN curl https://releases.hashicorp.com/terraform/$TERRAFORM_VERSION/terraform_${TERRAFORM_VERSION}_linux_amd64.zip -o /tmp/terraform.zip && \
  unzip -d /usr/local/bin /tmp/terraform.zip && \
  rm /tmp/terraform.zip

ARG AWSCLI_VERSION=2.34.45
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64-$AWSCLI_VERSION.zip" -o "awscliv2.zip" && \
  unzip awscliv2.zip && \
  ./aws/install && \
  rm -rf aws awscliv2.zip

RUN mkdir /root/.ssh
COPY known_hosts /root/.ssh/known_hosts

RUN mkdir /root/.aws
COPY config /root/.aws/config

RUN mkdir /root/.docker

RUN mkdir -p /opt/build
COPY run.sh /opt/build/run.sh
COPY run-test.sh /opt/build/run-test.sh
COPY build_functions.sh /opt/build/build_functions.sh

WORKDIR /opt/build
ENTRYPOINT ["./run.sh"]
