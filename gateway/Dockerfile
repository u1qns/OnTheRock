FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY build/libs/gateway-0.0.1-SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java", "-jar", "/gateway.jar"]