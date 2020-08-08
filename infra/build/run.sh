#!/bin/bash
set -e

source /opt/build/build_functions.sh
source /opt/build/run_envars.sh

set +x
setup_credentials "$1"
set -x

git clone "$_GIT_REPO"
cd "$_PROJECT_NAME"

build_push_application "$_DOCKER_IMAGE_NAME"

/opt/build/run-test.sh "$1"

tf_backend_init "$_TFSTATE_BUCKET_NAME"
tf_prod_apply "infra/tf"
set +x
setup_application_configuration "$1" "infra/tf"
set -x
ansible_deploy "infra/tf"