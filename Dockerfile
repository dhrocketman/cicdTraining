FROM tomcat:9.0

MAINTAINER Arvind Balaji

COPY target/*.war /usr/local/tomcat/webapps/


