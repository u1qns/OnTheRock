FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/sender-0.0.1-SNAPSHOT.jar sender.jar
ENTRYPOINT ["java", "-jar", "/sender.jar"]