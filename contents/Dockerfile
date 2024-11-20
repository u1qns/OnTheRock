FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY build/libs/contents-0.0.1-SNAPSHOT.jar contents.jar
COPY crawler/ ./crawler/

EXPOSE 9090

ENV TZ Asia/Seoul
ENTRYPOINT ["java", "-jar", "/contents.jar"]
