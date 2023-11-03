FROM maven:3.8.3-openjdk-17 as build

COPY . /opt/app

WORKDIR /opt/app

RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim

ARG JAR_FILE=opt/app/target/*SNAPSHOT.jar

WORKDIR /opt/app

COPY --from=build $JAR_FILE messenger.jar

ENTRYPOINT ["java", "-jar", "messenger.jar"]