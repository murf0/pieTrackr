FROM phusion/baseimage:latest

MAINTAINER mikael.mellgren@ferrologic.se

EXPOSE 80
EXPOSE 443

ENV DBG=TRUE

ENV env_mqttTopic=
ENV env_mqttServer=
ENV env_mqttPort=
ENV env_mqttClientid=
ENV env_mqttRepublish=
ENV env_mqttKeystore=
ENV env_mqttKeystorePW=
ENV env_mqttClean=true
ENV env_mqttUsername=
ENV env_mqttPassword=
ENV env_sqlUrl=
ENV env_sqlUser=
ENV env_sqlPassword=

COPY build.sh /build.sh
RUN chmod 755 /build.sh
RUN /build.sh

COPY setup.sh /etc/my_init.d/setup.sh
RUN chmod 755 /etc/my_init.d/setup.sh

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["/sbin/my_init"]