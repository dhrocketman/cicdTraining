FROM tomcat:8.0

COPY *.war /usr/local/tomcat/webapps/

EXPOSE 8085
CMD ["catalina.sh", "run"]
