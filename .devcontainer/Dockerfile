FROM ubuntu:22.04

ARG HTTP_PROXY 
ARG DEV_USERNAME
ARG USER_UID
ARG USER_GID

ENV DEBIAN_FRONTEND=noninteractive    
ENV http_proxy=${HTTP_PROXY}
ENV https_proxy=${HTTP_PROXY}
ENV HTTP_PROXY=${HTTP_PROXY}
ENV HTTPS_PROXY=${HTTP_PROXY}
ENV ALL_PROXY=${HTTP_PROXY}
ENV CLANGD_VERSION=21.1.0
ENV CLANGD_HOME=/usr/local/lib/clangd
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV MAVEN_HOME=/usr/local/maven
ENV MAVEN_VERSION=3.9.12
ENV LDB_TOOLCHAIN=/var/local/ldb-toolchain
ENV PATH=${LDB_TOOLCHAIN}/bin:${JAVA_HOME}/bin:${CLANGD_HOME}/bin:${MAVEN_HOME}/bin:${PATH}
ENV TZ=Asia/Shanghai

RUN \ 
  # prepare apt
  sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
  sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
  echo 'Acquire::ForceIPv4 "true";' >> /etc/apt/apt.conf.d/1000-force-ipv4-transport && \    
  echo 'Acquire::Retries "100";' >> /etc/apt/apt.conf.d/80-retries && \
  echo "Acquire::http::Proxy \"${HTTP_PROXY}\";" >> /etc/apt/apt.conf.d/99-proxy && \
  echo "Acquire::https::Proxy \"${HTTPS_PROXY}\";" >> /etc/apt/apt.conf.d/99-proxy && \
  apt-get update -y && \    
  # basics    
  PKGS="software-properties-common" && \
  apt-get install -y ${PKGS} && \    
  apt-get update -y && \    
  PKGS="vim sudo locales git curl unzip wget zip iputils-ping iproute2 tzdata tree jq" && \    
  PKGS="${PKGS} net-tools telnet ccache linux-tools-common linux-tools-generic" && \    
  apt-get install -y ${PKGS} && \    
  locale-gen en_US.UTF-8 && \    
  echo ${TZ} > /etc/timezone && \
  ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
  dpkg-reconfigure tzdata && \
  # doris build deps
  PKGS="openjdk-17-jdk maven pkg-config cmake byacc flex automake libtool-bin bison zip unzip libncurses5-dev curl git ninja-build python3" && \
  PKGS="${PKGS} autoconf automake libtool autopoint" && \
  # misc
  PKGS="${PKGS} mysql-client" && \
  apt-get install -y ${PKGS} && \
  apt-get autoclean 

ADD tmp/ldb_toolchain_gen_025.sh /tmp/

RUN \
  bash /tmp/ldb_toolchain_gen_025.sh ${LDB_TOOLCHAIN} \
  && rm /tmp/ldb_toolchain_gen_025.sh

RUN \
  # clangd
  wget "https://github.com/clangd/clangd/releases/download/${CLANGD_VERSION}/clangd-linux-${CLANGD_VERSION}.zip" -O /tmp/clangd.zip && \
  unzip /tmp/clangd.zip -d ${CLANGD_HOME} && \
  mv ${CLANGD_HOME}/clangd_${CLANGD_VERSION}/* ${CLANGD_HOME} && \
  rm -rf ${CLANGD_HOME}/clangd_${CLANGD_VERSION} && \
  rm -rf /tmp/clangd.zip
# # maven
# TODO: fix later
# wget https://mirrors.aliyun.com/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz -O /tmp/maven.tar.gz && \
# tar -zxvf /tmp/maven.tar.gz -C /usr/local/ && \
# mv /usr/local/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
# rm -rf /tmp/maven.tar.gz 

COPY tmp/apache-maven-3.9.12-bin.tar.gz /tmp/maven.tar.gz

RUN \
  tar -zxvf /tmp/maven.tar.gz -C /usr/local/ && \
  mv /usr/local/apache-maven-${MAVEN_VERSION} ${MAVEN_HOME} && \
  rm -rf /tmp/maven.tar.gz

COPY settings.xml ${MAVEN_HOME}/conf

RUN \
  # proxy for wget
  echo "use_proxy = on" >> /etc/wgetrc && \
  echo "http_proxy = ${http_proxy}" >> /etc/wgetrc && \
  echo "https_proxy = ${http_proxy}" >> /etc/wgetrc && \
  # misc
  echo "* soft core unlimited" >>/etc/security/limits.conf && \
  mkdir /coredumps && \
  chmod o+rwx /coredumps

RUN \
  # Create the user
  groupadd --gid $USER_GID $DEV_USERNAME \
  && useradd --uid $USER_UID --gid $USER_GID -m $DEV_USERNAME \
  #
  # [Optional] Add sudo support. Omit if you don't need to install software after connecting.
  && echo $DEV_USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$DEV_USERNAME \
  && chmod 0440 /etc/sudoers.d/$DEV_USERNAME

ENV LC_ALL="en_US.UTF-8"
ENV LANG="en_US.UTF-8"

USER $DEV_USERNAME

# unset
ENV http_proxy=
ENV https_proxy=
