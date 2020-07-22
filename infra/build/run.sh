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

/opt/build/run-test.sh

tf_backend_init "$_TFSTATE_BUCKET_NAME"

tf_apply "infra/tf"

ansible_deploy "infra/tf"