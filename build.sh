#!/usr/bin/env bash

readonly GIT_BRANCH=${GITHUB_HEAD_REF:-$GITHUB_REF_NAME}
readonly DOCKER_IMAGE_TAG=$([[ $GIT_BRANCH == "master" ]] && echo -n "latest" || sed 's/[^a-zA-Z0-9]/-/g' <<< "$GIT_BRANCH")
readonly IMAGE_NAME="scottg489/debatable-backend-build:$DOCKER_IMAGE_TAG"
readonly RUN_TASK=$1
readonly ID_RSA=$2
readonly DOCKER_CONFIG=$3
readonly AWS_CREDENTIALS=$4
readonly MAIN_KEY_PAIR=$5
readonly TWILIO_ACCOUNT_SID=$6
readonly TWILIO_API_KEY=$7
readonly TWILIO_API_SECRET=$8
readonly TWILIO_CHAT_SERVICE_SID=$9
readonly AWS_ACCESS_KEY_ID=${10}
readonly AWS_SECRET_ACCESS_KEY=${11}

read -r -d '' JSON_BODY <<- EOM
  {
  "RUN_TASK": "$RUN_TASK",
  "GIT_BRANCH": "$GIT_BRANCH",
  "DOCKER_IMAGE_TAG": "$DOCKER_IMAGE_TAG",
  "ID_RSA": "$ID_RSA",
  "DOCKER_CONFIG": "$DOCKER_CONFIG",
  "AWS_CREDENTIALS": "$AWS_CREDENTIALS",
  "MAIN_KEY_PAIR": "$MAIN_KEY_PAIR",
  "TWILIO_ACCOUNT_SID": "$TWILIO_ACCOUNT_SID",
  "TWILIO_API_KEY": "$TWILIO_API_KEY",
  "TWILIO_API_SECRET": "$TWILIO_API_SECRET",
  "TWILIO_CHAT_SERVICE_SID": "$TWILIO_CHAT_SERVICE_SID",
  "AWS_ACCESS_KEY_ID": "$AWS_ACCESS_KEY_ID",
  "AWS_SECRET_ACCESS_KEY": "$AWS_SECRET_ACCESS_KEY"
  }
EOM

curl -v -sS -w '\n%{http_code}' \
  --data-binary "$JSON_BODY" \
  "http://api.conjob.io/job/run?image=$IMAGE_NAME&remove=true&remove_image=true" \
  | tee /tmp/foo \
  | sed '$d' && \
  [ "$(tail -1 /tmp/foo)" -eq 200 ]
