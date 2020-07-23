#!/usr/bin/env bash

readonly IMAGE_NAME='scottg489/debatable-backend-build:latest'
readonly ID_RSA=$1
readonly DOCKER_CONFIG=$2
readonly AWS_CREDENTIALS=$3
readonly MAIN_KEY_PAIR=$4
readonly TWILIO_ACCOUNT_SID=$5
readonly TWILIO_API_KEY=$6
readonly TWILIO_API_SECRET=$7
readonly TWILIO_CHAT_SERVICE_SID=$8

read -r -d '' JSON_BODY <<- EOM
  {
  "ID_RSA": "$ID_RSA",
  "DOCKER_CONFIG": "$DOCKER_CONFIG",
  "AWS_CREDENTIALS": "$AWS_CREDENTIALS",
  "MAIN_KEY_PAIR": "$MAIN_KEY_PAIR",
  "TWILIO_ACCOUNT_SID": "$TWILIO_ACCOUNT_SID",
  "TWILIO_API_KEY": "$TWILIO_API_KEY",
  "TWILIO_API_SECRET": "$TWILIO_API_SECRET",
  "TWILIO_CHAT_SERVICE_SID": "$TWILIO_CHAT_SERVICE_SID"
  }
EOM

curl -v -sS -w '%{http_code}' \
  --data-binary "$JSON_BODY" \
  "http://api.simple-ci.com/build?image=$IMAGE_NAME" \
  | tee /tmp/foo \
  | sed '$d' && \
  [ "$(tail -1 /tmp/foo)" -eq 200 ]
