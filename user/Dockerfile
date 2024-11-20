FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/user-0.0.1-SNAPSHOT.jar user.jar
ENTRYPOINT ["java", "-jar", "/user.jar"]