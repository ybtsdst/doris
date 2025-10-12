FROM ubuntu:22.04

ARG OUTPUT_DIR
ARG HTTP_PROXY

ENV DEBIAN_FRONTEND=noninteractive    
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=${JAVA_HOME}/bin:${PATH}
ENV TZ=Asia/Shanghai
ENV GO_VERSION=1.25.1

RUN \
  # prepare apt
  sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
  sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
  echo 'Acquire::ForceIPv4 "true";' >> /etc/apt/apt.conf.d/1000-force-ipv4-transport && \    
  echo 'Acquire::Retries "100";' >> /etc/apt/apt.conf.d/80-retries && \
  apt-get update -y && \    
  # basics    
  PKGS="software-properties-common" && \
  PKGS="${PKGS} openjdk-17-jdk" && \
  PKGS="${PKGS} vim sudo locales curl unzip wget iputils-ping iproute2 tzdata tree" && \    
  # from doris
  PKGS="${PKGS} patchelf gdb binutils binutils-common mysql-client" \
  PKGS="${PKGS} curl wget less vim htop iproute2 numactl jq iotop sysstat" \
  PKGS="${PKGS} tcpdump iputils-ping dnsutils strace lsof blktrace tzdata" \
  PKGS="${PKGS} bpfcc-tools linux-headers-realtime linux-tools-realtime silversearcher-ag" \
  PKGS="${PKGS} net-tools ca-certificates openssl" && \
  apt-get install -y ${PKGS} && \
  apt-get autoclean && \
  rm -rf /var/lib/apt/lists/* && \
  locale-gen en_US.UTF-8 && \    
  echo ${TZ} > /etc/timezone && \
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
  dpkg-reconfigure tzdata

ENV LC_ALL="en_US.UTF-8"
ENV LANG="en_US.UTF-8"

# pprof
RUN \
  # proxy for wget
  echo "use_proxy = on" >> /root/.wgetrc && \
  echo "http_proxy = ${HTTP_PROXY}" >> /root/.wgetrc && \
  echo "https_proxy = ${HTTP_PROXY}" >> /root/.wgetrc && \
  wget https://go.dev/dl/go${GO_VERSION}.linux-amd64.tar.gz && \
  rm -rf /usr/local/go && \
  tar -C /usr/local -xzf go${GO_VERSION}.linux-amd64.tar.gz && \
  rm go${GO_VERSION}.linux-amd64.tar.gz && \
  echo "" > /root/.wgetrc

ENV PATH="/usr/local/go/bin:${PATH}"
ENV GOPATH=/go
ENV PATH="${GOPATH}/bin:${PATH}"

RUN GOPROXY=https://goproxy.cn,direct http_proxy=${HTTP_PROXY} http_proxy=${HTTP_PROXY} go install github.com/google/pprof@latest

COPY ${OUTPUT_DIR} /opt/doris

WORKDIR /opt/doris
