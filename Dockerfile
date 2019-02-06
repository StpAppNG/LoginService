FROM library/openjdk:11-jre

WORKDIR /app

ARG LOGINSERVICE_VERSION=1.3
ADD ./target/LoginService-${LOGINSERVICE_VERSION}-jar-with-dependencies.jar /app/LoginService.jar

EXPOSE 8080
CMD ["java", "-jar", "/app/LoginService.jar"]
