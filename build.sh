#!/usr/bin/env sh
if [ -z ${DBG} ]; then set -e; else set -ex; fi
export TERM=dumb
export PATH=/home/apigee/edgemicro­cli/bin:/usr/local/bin:/usr/local/sbin:$PATH
export DEBIAN_FRONTEND=noninteractive

cd /tmp

#Install Java
add-apt-repository ppa:webupd8team/java
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get update
apt-get -y install oracle-java8-installer oracle-java8-set-default git maven

#mkdir -p /etc/service/ /etc/my_init.d



apt-get -y upgrade
apt-get -y autoremove
rm -- "$0"