#!/usr/bin/env sh
if [ -z ${DBG} ]; then set -e; else set -ex; fi
export TERM=dumb
export DEBIAN_FRONTEND=noninteractive

cd /tmp

#See http://ubuntuhandbook.org/index.php/2015/01/install-openjdk-8-ubuntu-14-04-12-04-lts/

add-apt-repository ppa:openjdk-r/ppa

#Install Java
apt-get update
apt-get -y --no-install-recommends upgrade
apt-get -y --no-install-recommends install unzip git maven openjdk-8-jre openjdk-8-jdk
apt-get -y remove openjdk-7-jre openjdk-7-jre-headless
update-alternatives --config javac
update-alternatives --config java
java -version

#mkdir -p /etc/service/ /etc/my_init.d
apt-get -y autoremove
rm -- "$0"