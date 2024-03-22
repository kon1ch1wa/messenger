FROM maven:3.8.3-openjdk-17 as build
WORKDIR /app
COPY . .
RUN mvn package -Dmaven.test.skip -DskipTests
FROM openjdk:17.0.1-jdk-slim
ARG JAR_FILE=app/target/*SNAPSHOT.jar
WORKDIR /app
COPY --from=build ${JAR_FILE} messenger.jar
ENTRYPOINT [ "java", "-jar", "messenger.jar" ]