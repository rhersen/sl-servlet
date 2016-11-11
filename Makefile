install:	undeploy pull /opt/apache-tomcat-8.0.24/webapps/sj.war

pull:
	git pull

undeploy:
	rm -Rf /opt/apache-tomcat-8.0.24/webapps/sj*

build/libs/sl-servlet-1.0-SNAPSHOT.war:	$(shell find src/main/java/user -type f)
	gradle test war

/opt/apache-tomcat-8.0.24/webapps/sj.war:	build/libs/sl-servlet-1.0-SNAPSHOT.war
	mv build/libs/sl-servlet-1.0-SNAPSHOT.war /opt/apache-tomcat-8.0.24/webapps/sj.war
