#!/bin/bash
set -e

get_git_root_dir() {
  echo -n "$(git rev-parse --show-toplevel)"
}

setup_credentials() {
  set +x
  local ID_RSA_CONTENTS
  local MAINKEYPAIR_CONTENTS
  local AWS_CREDENTIALS_CONTENTS
  local DOCKER_CONFIG_CONTENTS

  readonly ID_RSA_CONTENTS=$(echo -n "$1" | jq -r .ID_RSA | base64 --decode)
  readonly MAINKEYPAIR_CONTENTS=$(echo -n "$1" | jq -r .MAIN_KEY_PAIR | base64 --decode)
  readonly AWS_CREDENTIALS_CONTENTS=$(echo -n "$1" | jq -r .AWS_CREDENTIALS | base64 --decode)
  readonly DOCKER_CONFIG_CONTENTS=$(echo -n "$1" | jq -r .DOCKER_CONFIG | base64 --decode)
  [[ -n $ID_RSA_CONTENTS ]]
  [[ -n $MAINKEYPAIR_CONTENTS ]]
  [[ -n $AWS_CREDENTIALS_CONTENTS ]]
  [[ -n $DOCKER_CONFIG_CONTENTS ]]

  printf -- "$ID_RSA_CONTENTS" >/root/.ssh/id_rsa
  printf -- "$MAINKEYPAIR_CONTENTS" >/root/.ssh/mainkeypair.pem
  printf -- "$AWS_CREDENTIALS_CONTENTS" >/root/.aws/credentials
  printf -- "$DOCKER_CONFIG_CONTENTS" >/root/.docker/config.json

  chmod 400 /root/.ssh/id_rsa
  chmod 400 /root/.ssh/mainkeypair.pem
}

build_test() {
  local ROOT_DIR
  readonly ROOT_DIR=$(get_git_root_dir)
  cd "$ROOT_DIR"

  ./gradlew --info build unitTest install

  docker build -t scottg489/debatable-backend .
}

push_application() {
  declare -r DOCKER_IMAGE_TAG=$1
  docker tag scottg489/debatable-backend scottg489/debatable-backend:$DOCKER_IMAGE_TAG
  docker push scottg489/debatable-backend:$DOCKER_IMAGE_TAG
}

tf_backend_init() {
  local ROOT_DIR
  local TFSTATE_BACKEND_BUCKET_NAME
  readonly ROOT_DIR=$(get_git_root_dir)
  readonly TFSTATE_BACKEND_BUCKET_NAME=$1

  cd "$ROOT_DIR/infra/tf/backend-init"

  # Initialize terraform backend on first deploy
  aws s3 ls "$TFSTATE_BACKEND_BUCKET_NAME" &&
    (terraform init &&
      terraform import aws_s3_bucket.backend_bucket "$TFSTATE_BACKEND_BUCKET_NAME")
  terraform init
  terraform plan
  terraform apply --auto-approve
}

tf_apply() {
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly RELATIVE_PATH_TO_TF_DIR=$1

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  terraform init
  terraform plan
  terraform apply --auto-approve
}

# TODO: This and the import we need to do is a hack. It is needed because this project share the
# TODO:   same hosted zone. We need to add the api subdomain but can't manage the zone ourselves
# TODO:   because the frontend project does. Need to figure out a way to handle this better
tf_prod_apply() {
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR
  local EXISTING_ZONE_ID

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly RELATIVE_PATH_TO_TF_DIR=$1

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  terraform init

  readonly EXISTING_ZONE_ID=$(aws route53 list-hosted-zones-by-name --max-items 1 --dns-name debate-table.com \
  | jq --raw-output '.HostedZones[0].Id')
  [[ -n $EXISTING_ZONE_ID ]]
  terraform import module.debatable_backend.aws_route53_zone.r53_zone "$EXISTING_ZONE_ID" || true

  terraform plan
  terraform apply --auto-approve
}

setup_application_configuration() {
  set +x
  [[ -n $1 ]]
  [[ -n $2 ]]
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR
  local BUILD_SCRIPT_JSON_INPUT
  local TWILIO_ACCOUNT_SID
  local TWILIO_API_KEY
  local TWILIO_API_SECRET
  local TWILIO_CHAT_SERVICE_SID
  local AWS_ACCESS_KEY_ID
  local AWS_SECRET_ACCESS_KEY
  local DYNAMODB_TABLE

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly BUILD_SCRIPT_JSON_INPUT=$1
  readonly RELATIVE_PATH_TO_TF_DIR=$2

  readonly TWILIO_ACCOUNT_SID=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .TWILIO_ACCOUNT_SID | base64 --decode)
  readonly TWILIO_API_KEY=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .TWILIO_API_KEY | base64 --decode)
  readonly TWILIO_API_SECRET=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .TWILIO_API_SECRET | base64 --decode)
  readonly TWILIO_CHAT_SERVICE_SID=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .TWILIO_CHAT_SERVICE_SID | base64 --decode)
  [[ -n $TWILIO_ACCOUNT_SID ]]
  [[ -n $TWILIO_API_KEY ]]
  [[ -n $TWILIO_API_SECRET ]]
  [[ -n $TWILIO_CHAT_SERVICE_SID ]]

  readonly AWS_ACCESS_KEY_ID=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .AWS_ACCESS_KEY_ID | base64 --decode)
  readonly AWS_SECRET_ACCESS_KEY=$(echo -n "$BUILD_SCRIPT_JSON_INPUT" | jq -r .AWS_SECRET_ACCESS_KEY | base64 --decode)
  [[ -n $AWS_ACCESS_KEY_ID ]]
  [[ -n $AWS_SECRET_ACCESS_KEY ]]


  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  readonly DYNAMODB_TABLE=$(terraform show --json | jq --raw-output '.values.outputs.dynamodb_table.value')
  [[ -n $DYNAMODB_TABLE ]]

  # These are used in the ansible playbook
  export _TWILIO_ACCOUNT_SID=$TWILIO_ACCOUNT_SID
  export _TWILIO_API_KEY=$TWILIO_API_KEY
  export _TWILIO_API_SECRET=$TWILIO_API_SECRET
  export _TWILIO_CHAT_SERVICE_SID=$TWILIO_CHAT_SERVICE_SID
  export _AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
  export _AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
  export _DYNAMODB_TABLE=$DYNAMODB_TABLE
}

ansible_deploy() {
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR
  local PUBLIC_IP
  local DOCKER_IMAGE_TAG

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly RELATIVE_PATH_TO_TF_DIR=$1
  readonly DOCKER_IMAGE_TAG=$2

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  readonly PUBLIC_IP=$(terraform show --json | jq --raw-output '.values.outputs.instance_public_ip.value')
  [[ -n $PUBLIC_IP ]]

  cd "$ROOT_DIR/infra/ansible"

  export _APP_DOCKER_IMAGE_TAG=$DOCKER_IMAGE_TAG

  ansible-playbook -v -u ubuntu -e ansible_ssh_private_key_file=/root/.ssh/mainkeypair.pem --inventory "$PUBLIC_IP", master-playbook.yml
}

run_tests() {
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR
  local PUBLIC_IP

  readonly RELATIVE_PATH_TO_TF_DIR=$1
  readonly ROOT_DIR=$(get_git_root_dir)

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"
  readonly PUBLIC_IP=$(terraform show --json | jq --raw-output '.values.outputs.instance_public_ip.value')
  [[ -n $PUBLIC_IP ]]

  cd "$ROOT_DIR"

  # Acceptance test configuration
  echo "baseUri=http://${PUBLIC_IP}:80" >"$ROOT_DIR/src/test/acceptance/resource/config.properties"
  echo "adminBaseUri=http://${PUBLIC_IP}:8081" >>"$ROOT_DIR/src/test/acceptance/resource/config.properties"
  # Performance test configuration
  echo "baseUri=http://${PUBLIC_IP}:80" >"$ROOT_DIR/src/test/performance/resources/config.properties"
  echo "adminBaseUri=http://${PUBLIC_IP}:8081" >>"$ROOT_DIR/src/test/performance/resources/config.properties"

  #./gradlew --info acceptanceTest performanceTest
  #./gradlew --info acceptanceTest
}
