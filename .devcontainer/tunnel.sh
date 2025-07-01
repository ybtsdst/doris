#!/bin/bash

client_ip=$(echo $SSH_CLIENT | awk '{print $1}')
local_port=18080
remote_port=58591

echo ${client_ip}

ssh -f -N -L ${local_port}:localhost:${remote_port} ybtsdst@${client_ip}

echo "tunnel created: ${local_port}-> ${client_ip}:${remote_port}"
