#!/bin/bash

common_build_args="
  --network=host
  --build-arg http_proxy=http://127.0.0.1:18080
  --build-arg https_proxy=http://127.0.0.1:18080
"

# common_build_args="
#   --network=host
#   --build-arg http_proxy=http://172.16.238.185:58591
#   --build-arg https_proxy=http://172.16.238.185:58591
# "

common_runtime_args="
  --network=host
"

function image_suffix {
  commit=$(git rev-parse --short HEAD)
  current_date=$(date +%Y%m%d)

  echo "${current_date}_${commit}"

  unset commit
  unset current_date
}

# run this under project root directory
function build_dev_image {
  tag=$(image_suffix)

  build_args="
    ${common_build_args}
  "

  docker build \
    ${build_args} \
    -t doris_build:${tag} \
    -f .devcontainer/Dockerfile \
    .devcontainer/

  unset tag
  unset build_args
}

# run this under project root directory
function build_runtime_image {
  tag=$(image_suffix)

  build_args="
    ${common_runtime_args}
  "

  docker build \
    ${build_args} \
    -t doris_runtime:${tag} \
    -f .devcontainer/Dockerfile \
    .devcontainer/

  unset tag
  unset build_args
}
