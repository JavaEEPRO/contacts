FROM openjdk:11-jdk
ADD ./target/contacts.jar /contacts.jar
ENTRYPOINT ["java","-jar","/contacts.jar"]