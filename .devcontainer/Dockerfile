FROM ubuntu:22.04

ARG http_proxy    
ARG https_proxy

ENV DEBIAN_FRONTEND=noninteractive    
ENV http_proxy=${http_proxy}
ENV https_proxy=${https_proxy}
ENV HTTP_PROXY=${http_proxy}
ENV HTTPS_PROXY=${https_proxy}
ENV ALL_PROXY=${http_proxy}
ENV DORIS_BUILD_PYTHON_VERSION=python3
ENV DORIS_TOOLCHAIN=gcc
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=${JAVA_HOME}/bin:${PATH}

RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list
RUN \
  echo 'Acquire::ForceIPv4 "true";' >> /etc/apt/apt.conf.d/1000-force-ipv4-transport && \    
  echo 'Acquire::Retries "100";' >> /etc/apt/apt.conf.d/80-retries && \
  apt-get update -y && \    
  # basics    
  PKGS="software-properties-common" && \
  apt-get install -y ${PKGS} && \    
  add-apt-repository -y ppa:ubuntu-toolchain-r/ppa && \
  apt-get update -y && \    
  PKGS="vim sudo locales git curl unzip wget zip iputils-ping iproute2" && \    
  PKGS="${PKGS} gdb net-tools telnet ccache" && \    
  apt-get install -y ${PKGS} && \    
  locale-gen en_US.UTF-8 && \    
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/timezone && \    
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
  # doris build deps
  PKGS="openjdk-17-jdk build-essential maven pkg-config cmake byacc flex automake libtool-bin bison binutils-dev libiberty-dev zip unzip libncurses5-dev curl git ninja-build python3" && \
  PKGS="${PKGS} libsasl2-dev gcc-10 g++-10 autoconf automake libtool autopoint comerr-dev" && \
  PKGS="${PKGS} libkrb5-dev" && \
  apt-get install -y ${PKGS} && \    
  apt-get autoclean && apt-get autoremove && \    
  rm -rf /var/lib/apt/lists/*

ENV LC_ALL="en_US.UTF-8"    
ENV LANG="en_US.UTF-8"    

RUN \
  # proxy for wget
  echo "use_proxy = on" >> /root/.wgetrc && \
  echo "http_proxy = ${http_proxy}" >> /root/.wgetrc && \
  echo "https_proxy = ${https_proxy}" >> /root/.wgetrc

WORKDIR /opt/transwarp/doris

