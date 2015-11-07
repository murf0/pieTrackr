FROM phusion/baseimage:latest

MAINTAINER mikael@murf.se

EXPOSE 80
EXPOSE 443

ENV DBG=TRUE

COPY build.sh /build.sh
RUN chmod 755 /build.sh
RUN /build.sh

COPY setup.sh /etc/my_init.d/setup.sh
RUN chmod 755 /etc/my_init.d/setup.sh

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

CMD ["/sbin/my_init"]