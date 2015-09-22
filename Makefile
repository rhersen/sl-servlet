install:	undeploy pull /opt/apache-tomcat-8.0.24/webapps/sl.war

pull:
	git pull

undeploy:
	rm -Rf /opt/apache-tomcat-8.0.24/webapps/sl

build/libs/sl-servlet-1.0-SNAPSHOT.war:	$(shell find src/main/java/user -type f)
	gradle war

/opt/apache-tomcat-8.0.24/webapps/sl.war:	build/libs/sl-servlet-1.0-SNAPSHOT.war
	cp build/libs/sl-servlet-1.0-SNAPSHOT.war /opt/apache-tomcat-8.0.24/webapps/sl.war
