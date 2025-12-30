#!/bin/bash

function main {
  # need to reload vscode to enable cmake language server

  # enable coredump
  sudo sysctl -w kernel.core_pattern="/coredumps/core-%e-%s-%u-%g-%p-%t"

  mkdir -p $HOME/.config/ccache
  echo "cache_dir = /opt/ccache" >> $HOME/.config/ccache/ccache.conf
  echo "max_size = 20.0G" >> $HOME/.config/ccache/ccache.conf

  echo "unset http_proxy" >> $HOME/.bashrc
  echo "unset https_proxy" >> $HOME/.bashrc

  # replace container settings.json with our project settings.json
  pushd $HOME/.vscode-server/data/Machine
  rm -rf settings.json
  ln -s /opt/transwarp/doris/.devcontainer/settings.json settings.json
  popd
}

main $@
