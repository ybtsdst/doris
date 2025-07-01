FROM ubuntu:22.04

ARG http_proxy    
ARG https_proxy

ENV DEBIAN_FRONTEND=noninteractive    
ENV HTTP_PROXY=${http_proxy}
ENV HTTPS_PROXY=${https_proxy}
    
RUN \
  sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
  echo 'Acquire::ForceIPv4 "true";' >> /etc/apt/apt.conf.d/1000-force-ipv4-transport && \    
  apt-get update -y && \    
  # basics    
  PKGS="software-properties-common vim sudo locales git curl unzip wget zip locales iputils-ping" && \    
  apt-get install -y ${PKGS} && \    
  locale-gen en_US.UTF-8 && \    
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/timezone && \    
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

ENV LC_ALL="en_US.UTF-8"    
ENV LANG="en_US.UTF-8"    

RUN \
  apt install -y openjdk-17-jdk && \
  apt install -y build-essential maven cmake byacc flex automake libtool-bin bison binutils-dev libiberty-dev zip unzip libncurses5-dev curl git ninja-build python3 && \
  apt install -y software-properties-common && \
  add-apt-repository -y ppa:ubuntu-toolchain-r/ppa && \
  apt update -y && \
  apt install -y gcc-10 g++-10 && \
  apt install -y autoconf automake libtool autopoint && \
  apt-get autoclean && apt-get autoremove && \    
  rm -rf /var/lib/apt/lists/*    

