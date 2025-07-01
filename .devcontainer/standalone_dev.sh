#!/bin/bash

# #####################################################################
# a script to run a shiva local cluster: 1 master + 1 tserver + 1 webserver + 1 httpserver
# 
# Basic Usage: 
#   source standalone_dev.sh
#   prepare_conf
#   start_cluster
# #####################################################################

# base dir
local_cluster_base_dir=/opt/shiva_local_cluster
# bin path
shiva_bin=/opt/transwarp/shiva/build/bin
webserver_jar=/opt/transwarp/shiva/java/shiva-restful/target/shiva-restful-SHIVA-UNDEFINED-VERSOIN.jar
httpserver_jar=/opt/transwarp/shiva/java/shiva-httpserver/rest-high-level-6.7.2/target/rest-high-level-6.7.2-SHIVA-UNDEFINED-VERSOIN.jar
current_host=$(hostname)
current_ip=$(hostname -i)
manage_service_port=28000
master_service_port=29630
master_group=${current_ip}:${master_service_port}
# web ui
webserver_port=24567
# rest
httpserver_port=28902

function init_webserver_conf {
  mkdir -p ${local_cluster_base_dir}/webserver
  mkdir -p ${local_cluster_base_dir}/webserver/conf
  mkdir -p ${local_cluster_base_dir}/webserver/lib
  mkdir -p ${local_cluster_base_dir}/webserver/log

  # create log4j2.xml
  cat <<EOT >${local_cluster_base_dir}/webserver/conf/log4j2.xml
<?xml version="1.0" encoding="UTF-8"?>    
<Configuration monitorInterval="60">    
    <Appenders>    
        <Null name="NullAppender"/>    
    
  <RollingFile name="RollingFileAll" fileName="${local_cluster_base_dir}/webserver/log/shiva-restful-server.log"    
    filePattern="${local_cluster_base_dir}/webserver/log/shiva-restful-server.log.%i.gz">    
            <PatternLayout pattern="[%p] [%t] %d{yyyy-MM-dd HH:mm:ss} (%c:%L): %m%n"/>    
            <ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY"/>    
            <Policies>    
                <SizeBasedTriggeringPolicy size="100MB"/>    
            </Policies>    
            <DefaultRolloverStrategy max="5"/>    
        </RollingFile>    
    </Appenders>    
    
    <Loggers>    
        <Root level="DEBUG">    
            <AppenderRef ref="RollingFileAll"/>    
        </Root>    
    </Loggers>    
</Configuration>
EOT

}

function init_httpserver_conf {
  mkdir -p ${local_cluster_base_dir}/httpserver
  mkdir -p ${local_cluster_base_dir}/httpserver/conf
  mkdir -p ${local_cluster_base_dir}/httpserver/lib
  mkdir -p ${local_cluster_base_dir}/httpserver/log

  # create log4j2.xml
  cat <<EOT >${local_cluster_base_dir}/httpserver/conf/log4j2.xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration monitorInterval="60">
    <Appenders>
        <Null name="NullAppender"/>
        <RollingFile name="RollingFileAll" fileName="${local_cluster_base_dir}/httpserver/log/shiva-httpserver.log"
                     filePattern="${local_cluster_base_dir}/httpserver/log/shiva-httpserver.log.%i.gz">
            <PatternLayout pattern="[%p] [%t] %d{yyyy-MM-dd HH:mm:ss} (%c:%L): %m%n"/>
            <ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="256 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="40"/>
        </RollingFile>
        <RollingFile name="ErrorLogRollingFile" fileName="${local_cluster_base_dir}/httpserver/log/shiva-httpserver.log.ERROR"
                     filePattern="${local_cluster_base_dir}/httpserver/log/shiva-httpserver.log.ERROR.%i.gz">
            <ThresholdFilter level="ERROR" onMatch="ACCEPT" onMismatch="DENY"/>
            <PatternLayout pattern="[%p] [%t] %d{yyyy-MM-dd HH:mm:ss} (%c:%L): %m%n"/>
            <Policies>
                <SizeBasedTriggeringPolicy size="256 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="40"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="RollingFileAll"/>
            <AppenderRef ref="ErrorLogRollingFile"/>
        </Root>
    </Loggers>
</Configuration>
EOT

  # create elasticsearch.yml
  cat <<EOT >${local_cluster_base_dir}/httpserver/conf/elasticsearch.yml
http.port: ${httpserver_port}
network.bind_host: 0.0.0.0
http.bind_host: 0.0.0.0


node.name: localhost
node.attr.dc: dc1
scope.track_rest_request: false
scope.table_pattern_cache.expire.s: 30

# extra flags
EOT
}

function init_master_conf {
  mkdir -p ${local_cluster_base_dir}/master
  mkdir -p ${local_cluster_base_dir}/master/conf
  mkdir -p ${local_cluster_base_dir}/master/log
  mkdir -p ${local_cluster_base_dir}/master/data

  # create master.conf
  cat <<EOT >${local_cluster_base_dir}/master/conf/master.conf
[master]
data_path=${local_cluster_base_dir}/master/data
public_host=${current_host}

[rpc_service]
master_service_port=${master_service_port}
EOT

  # create master.flags
  cat <<EOT >${local_cluster_base_dir}/master/conf/master.flags
# log
--log_dir=${local_cluster_base_dir}/master/log
--minloglevel=0
--v=2
--retain_log_size=2
EOT

}

function init_tserver_conf {
  mkdir -p ${local_cluster_base_dir}/tserver
  mkdir -p ${local_cluster_base_dir}/tserver/conf
  mkdir -p ${local_cluster_base_dir}/tserver/log
  mkdir -p ${local_cluster_base_dir}/tserver/data-0
  mkdir -p ${local_cluster_base_dir}/tserver/data-1

  # create store.conf
  cat <<EOT >${local_cluster_base_dir}/tserver/conf/store.conf
[store-0]
data_dir=${local_cluster_base_dir}/tserver/data-0
capacity_units=1000

[store-1]    
data_dir=${local_cluster_base_dir}/tserver/data-1
capacity_units=1000
EOT

  # create tabletserver.conf
  cat <<EOT >${local_cluster_base_dir}/tserver/conf/tabletserver.conf
[tabletserver]
topology_conf=${local_cluster_base_dir}/tserver/conf/topology.conf
store_conf=${local_cluster_base_dir}/tserver/conf/store.conf

[rpc_service]
manage_service_port=${manage_service_port}
EOT

  # create tabletserver.flags
  cat <<EOT >${local_cluster_base_dir}/tserver/conf/tabletserver.flags
# log
--log_dir=${local_cluster_base_dir}/tserver/log
--minloglevel=0
--v=2
--retain_log_size=2

# just for test
--disable_store_data_on_root_fs=false
--bind_to_any_address=true
EOT

  # create topology.conf
  cat <<EOT >${local_cluster_base_dir}/tserver/conf/topology.conf
[topology]
public_host=${current_host}
rack=rackxx
tag=
EOT

}

function prepare_conf {
  init_tserver_conf
  init_master_conf
  init_webserver_conf
  init_httpserver_conf
}

# refer to docker/standalone/standalone-boot.sh
function start_master {
  nohup ${shiva_bin}/master_main \
    --conf=${local_cluster_base_dir}/master/conf/master.conf \
    --flagfile=${local_cluster_base_dir}/master/conf/master.flags \
    1>${local_cluster_base_dir}/master/master.out 2>&1 &
}

# refer to docker/standalone/standalone-boot.sh
function start_tserver {
  # export HEAPPROFILE=/tmp/tserver.hprof
  nohup ${shiva_bin}/tabletserver_main \
    --conf=${local_cluster_base_dir}/tserver/conf/tabletserver.conf \
    --token_dir=${local_cluster_base_dir}/tserver/conf \
    --master_group=${master_group} \
    --flagfile=${local_cluster_base_dir}/tserver/conf/tabletserver.flags \
    > ${local_cluster_base_dir}/tserver/tserver.out 2>&1 &
  # unset HEAPPROFILE

  if [ $# == 1 ]; then
    num=$1 
    
    for ((i = 1; i <= $num; i++)); do
      echo "start tserver[$i]"
      
      nohup ${shiva_bin}/tabletserver_main \
        --conf=${local_cluster_base_dir}/tserver${i}/conf/tabletserver.conf \
        --token_dir=${local_cluster_base_dir}/tserver${i}/conf \
        --master_group=${master_group} \
        --flagfile=${local_cluster_base_dir}/tserver${i}/conf/tabletserver.flags \
        > ${local_cluster_base_dir}/tserver${i}/tserver.out 2>&1 &
    done
  fi
}

# refer to docker/standalone/conf/shiva-restful.sh
function start_webserver {
  JAVA_OPTS=""

  JAVA_OPTS="${JAVA_OPTS}
      -Dmaster_group=${master_group}
      -Dhttp_port=${webserver_port}
      -Dlog4j2.configurationFile=${local_cluster_base_dir}/webserver/conf/log4j2.xml"    

  nohup java ${JAVA_OPTS} -cp ".:${webserver_jar}" io.transwarp.shiva2.rest.ShivaWebServer \
    > ${local_cluster_base_dir}/webserver/webserver.log 2>&1 &
}

# refer to docker/standalone/conf/shiva-http-server.sh
function start_httpserver {
  JAVA_OPTS=""

  JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
  JAVA_OPTS="${JAVA_OPTS}
      -Dmaster_group=${master_group}
      -Dhttp_port=${httpserver_port}
      -Dlog4j2.configurationFile=${local_cluster_base_dir}/httpserver/conf/log4j2.xml"    

  nohup java ${JAVA_OPTS} -cp ".:${httpserver_jar}" io.transwarp.shiva2.Node ${master_group} ${local_cluster_base_dir}/httpserver/conf \
    > ${local_cluster_base_dir}/httpserver/httpserver.log 2>&1 &
}

# refer to docker/standalone/standalone-boot.sh
function start_cluster {
  start_master

  echo "master started; sleep for 10 seconds..."
  sleep 10

  # init master
  ${shiva_bin}/shiva_tool \
    --cmd=check_and_init_master_group \
    --master_group=${master_group}

  echo "init master finished"

  start_tserver

  echo "tserver started"

  start_webserver

  echo "web server started"

  start_httpserver

  echo "http server started"
}
