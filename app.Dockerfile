FROM openjdk:17.0.1-jdk-slim
COPY ./target/messenger-*.jar /app/messenger.jar
WORKDIR /app
ENTRYPOINT [ "java", "-jar", "./messenger.jar" ]