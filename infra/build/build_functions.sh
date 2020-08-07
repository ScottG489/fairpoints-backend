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

  printf -- "$ID_RSA_CONTENTS" >/root/.ssh/id_rsa
  printf -- "$MAINKEYPAIR_CONTENTS" >/root/.ssh/mainkeypair.pem
  printf -- "$AWS_CREDENTIALS_CONTENTS" >/root/.aws/credentials
  printf -- "$DOCKER_CONFIG_CONTENTS" >/root/.docker/config.json

  chmod 400 /root/.ssh/id_rsa
  chmod 400 /root/.ssh/mainkeypair.pem
}

setup_token_server_creds() {
  set +x
  local ROOT_DIR
  local TWILIO_ACCOUNT_SID
  local TWILIO_API_KEY
  local TWILIO_API_SECRET
  local TWILIO_CHAT_SERVICE_SID

  readonly ROOT_DIR=$(get_git_root_dir)

  readonly TWILIO_ACCOUNT_SID=$(echo -n $1 | jq -r .TWILIO_ACCOUNT_SID | base64 --decode)
  readonly TWILIO_API_KEY=$(echo -n $1 | jq -r .TWILIO_API_KEY | base64 --decode)
  readonly TWILIO_API_SECRET=$(echo -n $1 | jq -r .TWILIO_API_SECRET | base64 --decode)
  readonly TWILIO_CHAT_SERVICE_SID=$(echo -n $1 | jq -r .TWILIO_CHAT_SERVICE_SID | base64 --decode)

  {
    echo "twilio_account_sid: $TWILIO_ACCOUNT_SID"
    echo "twilio_api_key: $TWILIO_API_KEY"
    echo "twilio_api_secret: $TWILIO_API_SECRET"
    echo "twilio_chat_service_sid: $TWILIO_CHAT_SERVICE_SID"
  } >> "$ROOT_DIR/infra/ansible/vars.yml"
}

build_push_application() {
  local ROOT_DIR
  local DOCKER_IMAGE_NAME
  readonly ROOT_DIR=$(get_git_root_dir)
  readonly DOCKER_IMAGE_NAME=$1

  cd "$ROOT_DIR"

  ./gradlew --info build unitTest install

  docker build -t "$DOCKER_IMAGE_NAME" .
  docker push "$DOCKER_IMAGE_NAME"
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
  local ROOT_DIR
  local DYNAMODB_TABLE
  local RELATIVE_PATH_TO_TF_DIR

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly RELATIVE_PATH_TO_TF_DIR=$1

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  readonly DYNAMODB_TABLE=$(terraform show --json | jq --raw-output '.values.outputs.dynamodb_table.value')
  [[ -n $DYNAMODB_TABLE ]]

  {
    echo "dynamodb_table: $DYNAMODB_TABLE"
  } >> "$ROOT_DIR/infra/ansible/vars.yml"
}

ansible_deploy() {
  local ROOT_DIR
  local RELATIVE_PATH_TO_TF_DIR
  local PUBLIC_IP

  readonly ROOT_DIR=$(get_git_root_dir)
  readonly RELATIVE_PATH_TO_TF_DIR=$1

  cd "$ROOT_DIR/$RELATIVE_PATH_TO_TF_DIR"

  readonly PUBLIC_IP=$(terraform show --json | jq --raw-output '.values.outputs.instance_public_ip.value')
  [[ -n $PUBLIC_IP ]]

  cd "$ROOT_DIR/infra/ansible"
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

  ./gradlew --info acceptanceTest performanceTest
}
