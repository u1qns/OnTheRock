FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/message-0.0.1-SNAPSHOT.jar message.jar
ENTRYPOINT ["java", "-jar", "/message.jar"]