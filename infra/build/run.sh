#!/bin/bash
set -e

source /opt/build/build_functions.sh
source /opt/build/run_envars.sh

set +x
setup_credentials "$1"
set -x

# Start the docker daemon. This is necessary when using the sysbox-runc container runtime rather than mounting docker.sock
dockerd > /var/log/dockerd.log 2>&1 &
sleep 3

git clone "$_GIT_REPO"
cd "$_PROJECT_NAME"

build_push_application "$_DOCKER_IMAGE_NAME"

set +x
/opt/build/run-test.sh "$1"
set -x

exit
tf_backend_init "$_TFSTATE_BUCKET_NAME"
tf_prod_apply "infra/tf"
set +x
setup_application_configuration "$1" "infra/tf"
set -x
ansible_deploy "infra/tf"
