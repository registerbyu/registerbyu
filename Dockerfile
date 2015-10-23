from java:8-jre

copy registerbyu.jar /usr/src/myapp/app.jar
WORKDIR /usr/src/myapp
CMD ["java", "-jar", "app.jar"]
