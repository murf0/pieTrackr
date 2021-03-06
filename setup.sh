#!/usr/bin/env sh
if [ -z ${DBG} ]; then set -e; else set -ex; fi

export TERM=dumb
export PATH=/home/apigee/edgemicro/cli/bin:/bin:/usr/local/bin:/usr/local/sbin:$PATH
export DEBIAN_FRONTEND=noninteractive

cd /home
git clone https://github.com/murf0/pieTrackr.git
cd pieTrackr
mvn --batch-mode verify dependency:copy-dependencies
#mvn package
RAND=$(openssl rand -base64 4 | tr -dc 'a-zA-Z0-9')
echo "mqttTopic=${env_mqttTopic}
mqttServer=${env_mqttServer}
mqttPort=${env_mqttPort}
mqttClientid=${env_mqttClientid}-${RAND}
mqttRepublish=${env_mqttRepublish}
mqttKeystore=${env_mqttKeystore}
mqttKeystorePW=${env_mqttKeystorePW}
mqttClean=${env_mqttClean}
mqttPassword=${env_mqttPassword}
mqttUsername=${env_mqttUsername}
sqlUrl=${env_sqlUrl}
sqlUser=${env_sqlUser}
sqlPassword=${env_sqlPassword}
logLevel=${DBG}" > client.config

mkdir -p /etc/service/pietracker
echo "#!/bin/sh
cd /home/pieTrackr
java -jar /home/pieTrackr/target/pietrackr-0.0.1-SNAPSHOT-jar-with-dependencies.jar" > /etc/service/pietracker/run
chmod 755 /etc/service/pietracker/run

rm -- "$0"