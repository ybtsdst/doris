#!/bin/bash

project_dir=/opt/transwarp/doris
be_output_dir=${project_dir}/output/be
fe_output_dir=${project_dir}/output/fe
cluster_config_file=/opt/transwarp/doris/.devcontainer/devtools/local_cluster.json
cluster_base_path=/opt/doris_local_cluster/cluster

be_port=19060
be_webserver_port=18040
be_heartbeat_service_port=19050
be_brpc_port=18060

fe_http_port=18030
fe_rpc_port=19020
fe_query_port=19030
fe_edit_log_port=19010

function get_cluster_config {
    local cluster_id=${1:-0}

    len=$(jq '.cluster | length' ${cluster_config_file})

    if [ "${cluster_id}" -lt 0 ] || [ "${cluster_id}" -ge "${len}" ]; then
        echo "cluster id ${cluster_id} is out of range! (length=${len})"
        exit 1
    fi

    fe_count=$(jq ".cluster[${cluster_id}].fe" ${cluster_config_file})
    be_count=$(jq ".cluster[${cluster_id}].be" ${cluster_config_file})
    echo "${fe_count} ${be_count}"
}

function prepare_single_be {
    local cluster_id=$1
    local be_id=$2
    local be_path=${cluster_base_path}${cluster_id}/be${be_id}
    local target_be_port=$((be_port + be_id))
    local target_webserver_port=$((be_webserver_port + be_id))
    local target_heartbeat_service_port=$((be_heartbeat_service_port + be_id))
    local target_brpc_port=$((be_brpc_port + be_id))

    mkdir -p ${be_path}

    if [ ! -e "${be_path}/bin" ]; then
        cp -r -p ${be_output_dir}/bin ${be_path}/bin
    fi

    if [ ! -e "${be_path}/conf" ]; then
        mkdir -p ${be_path}/conf
        cp -r -p ${project_dir}/conf/be.conf ${be_path}/conf/
        cp -r -p ${project_dir}/conf/lsan_suppr.conf ${be_path}/conf/
        cp -r -p ${project_dir}/conf/asan_suppr.conf ${be_path}/conf/
        cp -r -p ${project_dir}/conf/odbcinst.ini ${be_path}/conf/

        # update be conf
        sed -i '/# priority_networks/a priority_networks = 127.0.0.1' ${be_path}/conf/be.conf
        sed -i 's/^be_port = .*/be_port = '"${target_be_port}"'/' ${be_path}/conf/be.conf
        sed -i 's/^webserver_port = .*/webserver_port = '"${target_webserver_port}"'/' ${be_path}/conf/be.conf
        sed -i 's/^heartbeat_service_port = .*/heartbeat_service_port = '"${target_heartbeat_service_port}"'/' ${be_path}/conf/be.conf
        sed -i 's/^brpc_port = .*/brpc_port = '"${target_brpc_port}"'/' ${be_path}/conf/be.conf
    fi

    mkdir -p ${be_path}/connectors
    mkdir -p ${be_path}/jdbc_drivers
    mkdir -p ${be_path}/log
    mkdir -p ${be_path}/storage

    if [ -d "${be_output_dir}/dict" ] && [ ! -e "${be_path}/dict" ]; then
        ln -s "${be_output_dir}/dict" "${be_path}/dict"
    fi

    if [ -d "${be_output_dir}/lib" ] && [ ! -e "${be_path}/lib" ]; then
        ln -s "${be_output_dir}/lib" "${be_path}/lib"
    fi

    if [ -d "${be_output_dir}/tools" ] && [ ! -e "${be_path}/tools" ]; then
        ln -s "${be_output_dir}/tools" "${be_path}/tools"
    fi

    if [ -d "${be_output_dir}/www" ] && [ ! -e "${be_path}/www" ]; then
        ln -s "${be_output_dir}/www" "${be_path}/www"
    fi
}

function prepare_single_fe {
    local cluster_id=$1
    local fe_id=$2
    local fe_path=${cluster_base_path}${cluster_id}/fe${fe_id}
    local target_fe_http_port=$((fe_http_port + fe_id))
    local target_fe_rpc_port=$((fe_rpc_port + fe_id))
    local target_fe_query_port=$((fe_query_port + fe_id))
    local target_fe_edit_log_port=$((fe_edit_log_port + fe_id))

    mkdir -p ${fe_path}

    if [ ! -e "${fe_path}/bin" ]; then
        cp -r -p ${fe_output_dir}/bin ${fe_path}/bin
    fi

    if [ ! -e "${fe_path}/conf" ]; then
        mkdir -p ${fe_path}/conf
        mkdir -p ${fe_path}/ssl
        cp -r -p ${project_dir}/conf/fe.conf ${fe_path}/conf/
        cp -r -p ${project_dir}/conf/ldap.conf ${fe_path}/conf/

        # update fe conf
        sed -i '/# priority_networks/a priority_networks = 127.0.0.1' ${fe_path}/conf/fe.conf
        sed -i 's/^http_port = .*/http_port = '"${target_fe_http_port}"'/' ${fe_path}/conf/fe.conf
        sed -i 's/^rpc_port = .*/rpc_port = '"${target_fe_rpc_port}"'/' ${fe_path}/conf/fe.conf
        sed -i 's/^query_port = .*/query_port = '"${target_fe_query_port}"'/' ${fe_path}/conf/fe.conf
        sed -i 's/^edit_log_port = .*/edit_log_port = '"${target_fe_edit_log_port}"'/' ${fe_path}/conf/fe.conf
    fi

    mkdir -p ${fe_path}/connectors
    mkdir -p ${fe_path}/jdbc_drivers
    mkdir -p ${fe_path}/doris-meta
    mkdir -p ${fe_path}/log
    mkdir -p ${fe_path}/minidump
    mkdir -p ${fe_path}/temp_dir

    if [ -d "${fe_output_dir}/lib" ] && [ ! -e "${fe_path}/lib" ]; then
        ln -s "${fe_output_dir}/lib" "${fe_path}/lib"
    fi

    if [ -d "${fe_output_dir}/mysql_ssl_default_certificate" ] && [ ! -e "${fe_path}/mysql_ssl_default_certificate" ]; then
        ln -s "${fe_output_dir}/mysql_ssl_default_certificate" "${fe_path}/mysql_ssl_default_certificate"
    fi

    if [ -d "${fe_output_dir}/plugins" ] && [ ! -e "${fe_path}/plugins" ]; then
        ln -s "${fe_output_dir}/plugins" "${fe_path}/plugins"
    fi

    if [ -d "${fe_output_dir}/spark-dpp" ] && [ ! -e "${fe_path}/spark-dpp" ]; then
        ln -s "${fe_output_dir}/spark-dpp" "${fe_path}/spark-dpp"
    fi

    if [ -d "${fe_output_dir}/webroot" ] && [ ! -e "${fe_path}/webroot" ]; then
        ln -s "${fe_output_dir}/webroot" "${fe_path}/webroot"
    fi
}

function prepare_be {
    local cluster_id=$1
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i
    for((i=0; i<$be_count; i++)); do
      prepare_single_be ${cluster_id} ${i}
    done
}

function prepare_fe {
    local cluster_id=$1
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i=0
    for((i=0; i<$fe_count; i++)); do
      prepare_single_fe ${cluster_id} ${i}
    done
}

function prepare_conf {
    len=$(jq '.cluster | length' ${cluster_config_file})

    local i=0
    for((i=0; i<$len; i++)); do
        prepare_fe ${i}
        prepare_be ${i}
    done
}

function start_single_be {
    local cluster_id=$1
    local be_id=$2
    local be_path=${cluster_base_path}${cluster_id}/be${be_id}
    export DORIS_HOME=${be_path}
    bash ${DORIS_HOME}/bin/start_be.sh --daemon
}

function stop_single_be {
    local cluster_id=$1
    local be_id=$2
    local be_path=${cluster_base_path}${cluster_id}/be${be_id}
    export DORIS_HOME=${be_path}
    bash ${DORIS_HOME}/bin/stop_be.sh
}

function start_single_fe {
    local cluster_id=$1
    local fe_id=$2
    local fe_path=${cluster_base_path}${cluster_id}/fe${fe_id}
    export DORIS_HOME=${fe_path}
    bash ${DORIS_HOME}/bin/start_fe.sh --daemon
}

function stop_single_fe {
    local cluster_id=$1
    local fe_id=$2
    local fe_path=${cluster_base_path}${cluster_id}/fe${fe_id}
    export DORIS_HOME=${fe_path}
    bash ${DORIS_HOME}/bin/stop_fe.sh
}

function start_fe {
    local cluster_id=${1:-0}
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i=0
    for((i=0; i<$fe_count; i++)); do
      start_single_fe ${cluster_id} ${i}
    done
}

function stop_fe {
    local cluster_id=${1:-0}
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i
    for((i=0; i<$fe_count; i++)); do
      stop_single_fe ${cluster_id} ${i}
    done
}

function restart_fe {
    local cluster_id=${1:-0}

    stop_fe ${cluster_id}
    start_fe ${cluster_id}
}

function start_be {
    local cluster_id=${1:-0}
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i
    for((i=0; i<$be_count; i++)); do
      start_single_be ${cluster_id} ${i}
    done
}

function stop_be {
    local cluster_id=${1:-0}
    local fe_count=0
    local be_count=0
    read fe_count be_count <<< $(get_cluster_config ${cluster_id})

    local i
    for((i=0; i<$be_count; i++)); do
      stop_single_be ${cluster_id} ${i}
    done
}

function restart_be {
    local cluster_id=${1:-0}

    stop_be ${cluster_id}
    start_be ${cluster_id}
}

function start_cluster {
    local cluster_id=${1:-0}

    start_fe ${cluster_id}
    start_be ${cluster_id}
}

function stop_cluster {
    local cluster_id=${1:-0}

    stop_fe ${cluster_id}
    stop_be ${cluster_id}
}

function restart_cluster {
    local cluster_id=${1:-0}

    stop_cluster ${cluster_id}
    start_cluster ${cluster_id}
}
