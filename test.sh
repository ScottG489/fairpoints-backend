#!/bin/bash
set -e

[[ -f .env.local ]] && source .env.local

declare ID_RSA_CONTENTS_BASE64
declare AWS_CREDENTIALS_CONTENTS_BASE64
declare DOCKER_CONFIG_CONTENTS_BASE64
declare MAIN_KEY_PAIR_CONTENTS_BASE64
declare TWILIO_ACCOUNT_SID_BASE64
declare TWILIO_API_KEY_BASE64
declare TWILIO_API_SECRET_BASE64
declare TWILIO_CHAT_SERVICE_SID_BASE64
declare AWS_ACCESS_KEY_ID_BASE64
declare AWS_SECRET_ACCESS_KEY_BASE64
# Change the location of these files based on where they are on your system
ID_RSA_CONTENTS_BASE64=$(base64 ~/.ssh/id_rsa | tr -d '\n')
AWS_CREDENTIALS_CONTENTS_BASE64=$(base64 ~/.aws/credentials | tr -d '\n')
DOCKER_CONFIG_CONTENTS_BASE64=$(base64 ~/.docker/config.json | tr -d '\n')
MAIN_KEY_PAIR_CONTENTS_BASE64=$(base64 ~/.ssh/mainkeypair.pem | tr -d '\n')
TWILIO_ACCOUNT_SID_BASE64=$(printf %s "$TWILIO_ACCOUNT_SID" | base64 | tr -d '\n')
TWILIO_API_KEY_BASE64=$(printf %s "$TWILIO_API_KEY" | base64 | tr -d '\n')
TWILIO_API_SECRET_BASE64=$(printf %s "$TWILIO_API_SECRET" | base64 | tr -d '\n')
TWILIO_CHAT_SERVICE_SID_BASE64=$(printf %s "$TWILIO_CHAT_SERVICE_SID" | base64 | tr -d '\n')
AWS_ACCESS_KEY_ID_BASE64=$(printf %s "$AWS_ACCESS_KEY_ID" | base64 | tr -d '\n')
AWS_SECRET_ACCESS_KEY_BASE64=$(printf %s "$AWS_SECRET_ACCESS_KEY" | base64 | tr -d '\n')

[[ -n $ID_RSA_CONTENTS_BASE64 ]]
[[ -n $AWS_CREDENTIALS_CONTENTS_BASE64 ]]
[[ -n $DOCKER_CONFIG_CONTENTS_BASE64 ]]
[[ -n $MAIN_KEY_PAIR_CONTENTS_BASE64 ]]
[[ -n $TWILIO_ACCOUNT_SID_BASE64 ]]
[[ -n $TWILIO_API_KEY_BASE64 ]]
[[ -n $TWILIO_API_SECRET_BASE64 ]]
[[ -n $TWILIO_CHAT_SERVICE_SID_BASE64 ]]
[[ -n $AWS_ACCESS_KEY_ID_BASE64 ]]
[[ -n $AWS_SECRET_ACCESS_KEY_BASE64 ]]

read -r -d '\' JSON_BODY <<- EOM
  {
  "RUN_TASK": "test",
  "DOCKER_IMAGE_TAG": "local_test",
  "ID_RSA": "$ID_RSA_CONTENTS_BASE64",
  "DOCKER_CONFIG": "$DOCKER_CONFIG_CONTENTS_BASE64",
  "AWS_CREDENTIALS": "$AWS_CREDENTIALS_CONTENTS_BASE64",
  "MAIN_KEY_PAIR": "$MAIN_KEY_PAIR_CONTENTS_BASE64",
  "TWILIO_ACCOUNT_SID": "$TWILIO_ACCOUNT_SID_BASE64",
  "TWILIO_API_KEY": "$TWILIO_API_KEY_BASE64",
  "TWILIO_API_SECRET": "$TWILIO_API_SECRET_BASE64",
  "TWILIO_CHAT_SERVICE_SID": "$TWILIO_CHAT_SERVICE_SID_BASE64",
  "AWS_ACCESS_KEY_ID": "$AWS_ACCESS_KEY_ID_BASE64",
  "AWS_SECRET_ACCESS_KEY": "$AWS_SECRET_ACCESS_KEY_BASE64"
  }\\
EOM

LOCAL_IMAGE_TAG="debatable-backend-build-test-$(uuidgen | cut -c -8)"
docker build infra/build -t $LOCAL_IMAGE_TAG && \
docker run -it \
  --runtime=sysbox-runc \
  --volume "$PWD:/opt/build/debatable-backend" \
  $LOCAL_IMAGE_TAG "$JSON_BODY"
